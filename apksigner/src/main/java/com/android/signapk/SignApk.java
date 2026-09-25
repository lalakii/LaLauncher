package com.android.signapk;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.crypto.EncryptedPrivateKeyInfo;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.util.encoders.Base64;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;

public class SignApk {
	/**
	 * @author lalaki
	 */
	private static void writeSignatureBlock(byte[] var0, String algName, X509Certificate var1, OutputStream var2)
			throws IOException, GeneralSecurityException {
		ArrayList<java.security.cert.Certificate> certList = new ArrayList<java.security.cert.Certificate>();
		certList.add(var1);
		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		try {
			generator.setDefiniteLengthEncoding(true);
			generator.addCertificates(new org.bouncycastle.cert.jcajce.JcaCertStore(certList));
			generator.addSignerInfoGenerator(new org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder(
					new org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder().build())
					.setDirectSignature(true).build(new org.bouncycastle.operator.ContentSigner() {

						@Override
						public byte[] getSignature() {
							return var0;
						}

						@Override
						public OutputStream getOutputStream() {
							return new ByteArrayOutputStream();
						}

						@Override
						public org.bouncycastle.asn1.x509.AlgorithmIdentifier getAlgorithmIdentifier() {
							return new org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder()
									.find(algName);
						}

					}, var1));
			var2.write(generator.generate(new org.bouncycastle.cms.CMSProcessableByteArray(null)).getEncoded());
		} catch (org.bouncycastle.operator.OperatorCreationException | org.bouncycastle.cms.CMSException e) {
			e.fillInStackTrace();
		}
	}

	private static final Pattern stripPattern = Pattern.compile("^META-INF/(.*)[.](SF|RSA|DSA)$");

	private static X509Certificate readPublicKey(File var0) throws IOException, GeneralSecurityException {
		InputStream var1 = Files.asByteSource(var0).openStream();
		try {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(var1);
		} finally {
			var1.close();
		}
	}

	private static String readPassword(File var0) {
		System.out.print("Enter password for " + var0 + " (password will not be hidden): ");
		System.out.flush();
		java.util.Scanner var1 = new java.util.Scanner(System.in);
		try {
			return var1.nextLine();
		} finally {
			var1.close();
		}
	}

	private static KeySpec decryptPrivateKey(byte[] var0, File var1) throws GeneralSecurityException {
		EncryptedPrivateKeyInfo var2;
		try {
			var2 = new EncryptedPrivateKeyInfo(var0);
		} catch (IOException var9) {
			return null;
		}
		javax.crypto.Cipher var6 = javax.crypto.Cipher.getInstance(var2.getAlgName());
		var6.init(2, javax.crypto.SecretKeyFactory.getInstance(var2.getAlgName()).generateSecret(
				new javax.crypto.spec.PBEKeySpec(readPassword(var1).toCharArray())), var2.getAlgParameters());
		try {
			return var2.getKeySpec(var6);
		} catch (InvalidKeySpecException var8) {
			System.err.println("signapk: Password for " + var1 + " may be bad.");
			throw var8;
		}
	}

	private static PrivateKey readPrivateKey(File var0) throws IOException, GeneralSecurityException {
		byte[] var2 = Files.toByteArray(var0);
		KeySpec var3 = decryptPrivateKey(var2, var0);
		if (var3 == null) {
			var3 = new PKCS8EncodedKeySpec(var2);
		}
		PrivateKey var5 = null;
		for (String algName : new String[] { "RSA", "DSA" }) {
			try {
				var5 = java.security.KeyFactory.getInstance(algName).generatePrivate(var3);
				break;
			} catch (InvalidKeySpecException var9) {
			}
		}
		return var5;
	}

	private static Manifest addDigestsToManifest(JarFile var0, MessageDigest var5)
			throws IOException, GeneralSecurityException {
		Manifest var1 = var0.getManifest();
		Manifest var2 = new Manifest();
		Attributes var3 = var2.getMainAttributes();
		if (var1 != null) {
			var3.putAll(var1.getMainAttributes());
		} else {
			var3.putValue("Manifest-Version", "1.0");
			// var3.putValue("Created-By", "1.0 (Android SignApk)");
		}
		TreeMap<String, JarEntry> var8 = new TreeMap<String, JarEntry>();
		ImmutableSet.copyOf(Iterators.forEnumeration(var0.entries())).forEach(var9 -> var8.put(var9.getName(), var9));
		int num;
		byte[] buffer = new byte[4096];
		for (JarEntry entry : var8.values()) {
			String name = entry.getName();
			if (!entry.isDirectory() && !name.equals(JarFile.MANIFEST_NAME) && !name.equals("META-INF/_.SF")
					&& !name.equals("META-INF/_.RSA") && !name.equals("META-INF/com/android/otacert")
					&& (stripPattern == null || !stripPattern.matcher(name).matches())) {
				InputStream data = var0.getInputStream(entry);
				while ((num = data.read(buffer)) > 0) {
					var5.update(buffer, 0, num);
				}
				Attributes attr = null;
				if (var1 != null) {
					attr = var1.getAttributes(name);
				}
				attr = attr != null ? new Attributes(attr) : new Attributes();
				attr.putValue("SHA1-Digest", new String(Base64.encode(var5.digest()), "ASCII"));
				var2.getEntries().put(name, attr);
			}
		}
		return var2;
	}

	private static void writeSignatureFile(Manifest var0, MessageDigest var5, SignApk.SignatureOutputStream var1)
			throws IOException, GeneralSecurityException {
		Manifest var2 = new Manifest();
		Attributes var3 = var2.getMainAttributes();
		var3.putValue("Signature-Version", "1.0");
		// var3.putValue("Created-By", "1.0 (Android SignApk)");
		java.io.PrintStream var6 = new java.io.PrintStream(new DigestOutputStream(new ByteArrayOutputStream(), var5),
				true, "UTF-8");
		var0.write(var6);
		var6.flush();
		var3.putValue("SHA1-Digest-Manifest", Base64.toBase64String(var5.digest()));
		var0.getEntries().forEach((key, value) -> {
			var6.print("Name: " + key + "\r\n");
			value.forEach((key1, value1) -> {
				var6.print(key1 + ": " + value1 + "\r\n");
			});
			var6.print("\r\n");
			var6.flush();
			Attributes var12 = new Attributes();
			var12.putValue("SHA1-Digest", Base64.toBase64String(var5.digest()));
			var2.getEntries().put(key, var12);
		});
		var2.write(var1);
		if (var1.size() % 1024 == 0) {
			var1.write(13);
			var1.write(10);
		}

	}

	private static void signWholeOutputFile(byte[] var0, Signature var4, String algName, OutputStream var1,
			X509Certificate var2, PrivateKey var3) throws IOException, GeneralSecurityException {
		if (var0[var0.length - 22] == 80 && var0[var0.length - 21] == 75 && var0[var0.length - 20] == 5
				&& var0[var0.length - 19] == 6) {
			var4.initSign(var3);
			var4.update(var0, 0, var0.length - 2);
			ByteArrayOutputStream var5 = new ByteArrayOutputStream();
			byte[] var6 = "signed by SignApk".getBytes("UTF-8");
			var5.write(var6);
			var5.write(0);
			writeSignatureBlock(var4.sign(), algName, var2, var5);
			int var7 = var5.size() + 6;
			if (var7 > 65535) {
				throw new IllegalArgumentException("signature is too big for ZIP file comment");
			} else {
				int var8 = var7 - var6.length - 1;
				var5.write(var8 & 255);
				var5.write(var8 >> 8 & 255);
				var5.write(255);
				var5.write(255);
				var5.write(var7 & 255);
				var5.write(var7 >> 8 & 255);
				var5.flush();
				byte[] var9 = var5.toByteArray();

				for (int var10 = 0; var10 < var9.length - 3; ++var10) {
					if (var9[var10] == 80 && var9[var10 + 1] == 75 && var9[var10 + 2] == 5 && var9[var10 + 3] == 6) {
						throw new IllegalArgumentException("found spurious EOCD header at " + var10);
					}
				}

				var1.write(var0, 0, var0.length - 2);
				var1.write(var7 & 255);
				var1.write(var7 >> 8 & 255);
				var5.writeTo(var1);
			}
		} else {
			throw new IllegalArgumentException("zip data already has an archive comment");
		}
	}

	private static void copyFiles(Manifest var0, JarFile var1, JarOutputStream var2, long var3) throws IOException {
		byte[] var5 = new byte[4096];
		ArrayList<String> var8 = new ArrayList<String>(var0.getEntries().keySet());
		var8.sort(java.util.Comparator.naturalOrder());
		for (String var10 : var8) {
			JarEntry var11 = var1.getJarEntry(var10);
			JarEntry var12 = null;
			if (var11.getMethod() == 0) {
				var12 = new JarEntry(var11);
			} else {
				var12 = new JarEntry(var10);
			}
			var12.setTime(var3);
			var2.putNextEntry(var12);
			InputStream var13 = var1.getInputStream(var11);
			int var6;
			while ((var6 = var13.read(var5)) > 0) {
				var2.write(var5, 0, var6);
			}
			var2.flush();
		}
	}

	public static void main(String[] var0) {
		if (var0.length != 4 && var0.length != 5) {
			System.err.println("Usage: signapk [-w] publickey.x509[.pem] privatekey.pk8 input.apk output.apk");
			System.exit(2);
		}

		boolean var1 = false;
		byte var2 = 0;
		if (var0[0].equals("-w")) {
			var1 = true;
			var2 = 1;
		}

		JarFile var3 = null;
		JarOutputStream var4 = null;
		FileOutputStream var5 = null;

		try {
			X509Certificate var6 = readPublicKey(new File(var0[var2 + 0]));
			long var7 = var6.getNotBefore().getTime() + 3600000L;
			PrivateKey var9 = readPrivateKey(new File(var0[var2 + 1]));
			var3 = new JarFile(new File(var0[var2 + 2]), false);
			OutputStream var10 = null;
			if (var1) {
				var10 = new ByteArrayOutputStream();
			} else {
				var10 = var5 = new FileOutputStream(var0[var2 + 3]);
			}
			var4 = new JarOutputStream(var10);
			var4.setLevel(java.util.zip.Deflater.DEFAULT_COMPRESSION);
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			Manifest var12 = addDigestsToManifest(var3, digest);
			JarEntry var11 = new JarEntry(JarFile.MANIFEST_NAME);
			var11.setTime(var7);
			var4.putNextEntry(var11);
			var12.write(var4);
			var11 = new JarEntry("META-INF/_.SF");
			var11.setTime(var7);
			var4.putNextEntry(var11);
			Signature var13 = Signature.getInstance("SHA1withRSA");
			var13.initSign(var9);
			writeSignatureFile(var12, digest, new SignApk.SignatureOutputStream(var4, var13));
			var11 = new JarEntry("META-INF/_.RSA");
			var11.setTime(var7);
			var4.putNextEntry(var11);
			writeSignatureBlock(var13.sign(), var13.getAlgorithm(), var6, var4);
			copyFiles(var12, var3, var4, var7);
			var4.close();
			var4 = null;
			var10.flush();
			if (var10 instanceof ByteArrayOutputStream) {
				var5 = new FileOutputStream(var0[var2 + 3]);
				signWholeOutputFile(((ByteArrayOutputStream) var10).toByteArray(), var13, var13.getAlgorithm(), var5,
						var6, var9);
			}
		} catch (Exception var22) {
			var22.fillInStackTrace();
			System.exit(1);
		} finally {
			try {
				if (var3 != null) {
					var3.close();
				}

				if (var5 != null) {
					var5.close();
				}
			} catch (IOException var21) {
				var21.fillInStackTrace();
				System.exit(1);
			}
		}
	}

	private static class SignatureOutputStream extends BufferedOutputStream {
		private Signature mSignature;
		private int mCount;

		public SignatureOutputStream(OutputStream var1, Signature var2) {
			super(var1);
			this.mSignature = var2;
			this.mCount = 0;
		}

		public void write(int var1) throws IOException {
			try {
				this.mSignature.update((byte) var1);
			} catch (SignatureException var3) {
				throw new IOException("SignatureException: " + var3);
			}

			super.write(var1);
			++this.mCount;
		}

		public void write(byte[] var1, int var2, int var3) throws IOException {
			try {
				this.mSignature.update(var1, var2, var3);
			} catch (SignatureException var5) {
				throw new IOException("SignatureException: " + var5);
			}

			super.write(var1, var2, var3);
			this.mCount += var3;
		}

		public int size() {
			return this.mCount;
		}
	}
}