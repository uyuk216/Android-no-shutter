package dev.noshutter

import android.content.Context
import android.os.Build
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date

/**
 * ADB client that talks to this device's own adbd over Wireless debugging,
 * so the app can run [ShellMain] as the shell user without a PC.
 * The key pair is generated once and kept in the app's private files.
 */
class LocalAdb private constructor(context: Context) : AbsAdbConnectionManager() {
    private val privateKey: PrivateKey
    private val certificate: Certificate
    private val apkPath = context.applicationInfo.sourceDir

    init {
        setApi(Build.VERSION.SDK_INT)
        setHostAddress("127.0.0.1")
        val keyFile = File(context.filesDir, "adb.key")
        val certFile = File(context.filesDir, "adb.crt")
        if (keyFile.exists() && certFile.exists()) {
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyFile.readBytes()))
            certificate = certFile.inputStream().use { CertificateFactory.getInstance("X.509").generateCertificate(it) }
        } else {
            val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val name = X500Name("CN=NoShutter")
            val now = System.currentTimeMillis()
            val holder = JcaX509v3CertificateBuilder(
                name, BigInteger.valueOf(now), Date(now), Date(now + 10L * 365 * 86_400_000), name, pair.public,
            ).build(JcaContentSignerBuilder("SHA256withRSA").build(pair.private))
            privateKey = pair.private
            certificate = JcaX509CertificateConverter().getCertificate(holder)
            keyFile.writeBytes(privateKey.encoded)
            certFile.writeBytes(certificate.encoded)
        }
    }

    override fun getPrivateKey() = privateKey
    override fun getCertificate() = certificate
    override fun getDeviceName() = "NoShutter"

    /** One-time pairing with the code shown in "Pair device with pairing code". */
    fun pairWith(port: Int, code: String): Boolean = pair(port, code)

    /** Connects (discovering the Wireless debugging port via mDNS) and runs ShellMain. */
    fun run(context: Context, action: String): String {
        if (!isConnected && !connectTls(context, 10_000)) {
            error("ワイヤレスデバッグに接続できません。オンになっているか、ペア設定済みか確認してください。")
        }
        val cmd = "CLASSPATH=$apkPath app_process /system/bin dev.noshutter.ShellMain $action"
        return openStream("shell:$cmd").use { s -> s.openInputStream().bufferedReader().readText().trim() }
    }

    companion object {
        @Volatile private var instance: LocalAdb? = null
        fun get(context: Context): LocalAdb =
            instance ?: synchronized(this) { instance ?: LocalAdb(context.applicationContext).also { instance = it } }
    }
}
