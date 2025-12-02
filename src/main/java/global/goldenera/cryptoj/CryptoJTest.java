/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025-2030 The GoldenEraGlobal Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package global.goldenera.cryptoj;

import java.security.Security;

import org.apache.tuweni.bytes.Bytes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.datatypes.Signature;

/**
 * Test class to verify the functionality of the entire CryptoJ library.
 * Runs independently (has a 'main' method).
 */
public class CryptoJTest {

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	public static void main(String[] args) {
		System.out.println("=== Running CryptoJ Health Check ===");

		try {
			// Test 1: Generating keys and addresses
			runKeyAndAddressTest();

			// Test 2: Mnemonic and deterministic derivation
			runMnemonicTest();

			// Test 3: Signatures and verification
			runSignatureTest();

			System.out.println("\n✅ ALL TESTS PASSED SUCCESSFULLY.");

		} catch (Exception e) {
			System.err.println("\n❌ TEST FAILED!");
			e.printStackTrace();
		}
	}

	private static void runKeyAndAddressTest() throws Exception {
		System.out.println("\n--- TEST 1: Keys and Addresses ---");

		// 1. Generating
		System.out.println("Generating new key pair...");
		PrivateKey kp = PrivateKey.create();
		System.out.println("  Private key: " + kp.toHexString());

		// 2. Derivation of address
		Address addr = kp.getAddress();
		System.out.println("  Address (bytes): " + addr.toHexString());

		// 3. Encoding (EIP-55)
		String checksummedAddr = addr.toChecksumAddress();
		System.out.println("  Address (EIP-55): " + checksummedAddr);

		// 4. Decoding
		Address decodedAddr = Address.fromHexString(checksummedAddr);

		// 5. Verification
		assertEquals(addr, decodedAddr, "Address after decoding does not match!");
		System.out.println("  Address (decoded): " + decodedAddr.toHexString());
		System.out.println("  Verification of Encoding/Decoding: OK");

		// 6. Validation
		assertTrue(Address.isValid(checksummedAddr), "Validator failed (positive test)");
		assertFalse(Address.isValid("0xAb5801a7D398351b8bE11C439e05C5B3259aeC9G"),
				"Validator failed (negative test - wrong characters)");
		System.out.println("  Validation of addresses: OK");
	}

	private static void runMnemonicTest() throws Exception {
		System.out.println("\n--- TEST 2: Mnemonic (BIP-39) and Derivation (BIP-44) ---");

		// We use a fixed mnemonic for deterministic test
		String mnemonic = "bundle equip learn banner trash toast disease sick degree virtual rice wife";
		String password = "my-secret-password-123";
		System.out.println("  Using fixed Mnemonic: " + mnemonic);

		// 1. Derivation (index 0)
		System.out.println("  Deriving address for index 0...");
		PrivateKey kp0_A = PrivateKey.load(mnemonic, password, 0);
		Address addr0_A = kp0_A.getAddress();
		System.out.println("    -> " + addr0_A.toChecksumAddress());

		// 2. Derivation (index 0, again) + test helper method
		System.out.println("  Deriving address for index 0 (again, via helper method)...");
		PrivateKey kp0_B = PrivateKey.load(mnemonic, password); // Without index
		Address addr0_B = kp0_B.getAddress();
		System.out.println("    -> " + addr0_B.toChecksumAddress());

		// 3. Verification of determinism
		assertEquals(addr0_A, addr0_B, "Derivation for index 0 is not deterministic!");
		System.out.println("  Verification of determinism (index 0 == index 0): OK");

		// 4. Derivation (index 1)
		System.out.println("  Deriving address for index 1...");
		PrivateKey kp1 = PrivateKey.load(mnemonic, password, 1);
		Address addr1 = kp1.getAddress();
		System.out.println("    -> " + addr1.toChecksumAddress());

		// 5. Verification of uniqueness
		assertNotEquals(addr0_A, addr1, "Addresses for index 0 and 1 are the same!");
		System.out.println("  Verification of uniqueness (index 0 != index 1): OK");
	}

	private static void runSignatureTest() throws Exception {
		System.out.println("\n--- TEST 3: Signatures and Verification ---");

		// 1. Preparation (Keys and data)
		PrivateKey kpSigner = PrivateKey.create();
		Address addrSigner = kpSigner.getAddress();
		System.out.println("  Address for signature: " + addrSigner.toChecksumAddress());

		PrivateKey kpFake = PrivateKey.create();
		Address addrFake = kpFake.getAddress();

		Bytes data = Bytes
				.wrap("This is a secret message for the blockchain!".getBytes(java.nio.charset.StandardCharsets.UTF_8));

		// 2. Hashing (Full ETH Style)
		Hash messageHash = Hash.hash(data);
		System.out.println("  Hash of message (Keccak-256): " + messageHash.toHexString());

		// 3. Signing
		Signature signature = kpSigner.sign(messageHash);
		System.out.println("  Generated signature (65b): " + signature.toHexString().substring(0, 15) + "...");

		// 4. Verification (Validate) - Positive test
		System.out.println("  Verifying signature against correct address...");
		boolean isValid_Positive = signature.validate(messageHash, addrSigner);
		assertTrue(isValid_Positive, "Validation (positive) failed!");
		System.out.println("  Result: OK");

		// 5. Verification (Validate) - Negative test
		System.out.println("  Verifying signature against false address...");
		boolean isValid_Negative = signature.validate(messageHash, addrFake);
		assertFalse(isValid_Negative, "Validation (negative) failed!");
		System.out.println("  Result: OK");

		// 6. Recovering (Recover)
		System.out.println("  Recovering address from signature...");
		Address recoveredAddress = signature.recoverAddress(messageHash);
		System.out.println("  Recovered address: " + recoveredAddress.toChecksumAddress());

		// 7. Final verification
		assertEquals(addrSigner, recoveredAddress, "Recovered address does not match the original!");
		System.out.println("  Verification of recovery: OK");
	}

	// --- Simple helper functions for tests ---
	private static void assertEquals(Object a, Object b, String message) {
		if (!a.equals(b)) {
			throw new RuntimeException("ASSERT FAILED: " + message + " | Expected: " + a + ", Got: " + b);
		}
	}

	private static void assertNotEquals(Object a, Object b, String message) {
		if (a.equals(b)) {
			throw new RuntimeException("ASSERT FAILED: " + message + " | Values were equal: " + a);
		}
	}

	private static void assertTrue(boolean value, String message) {
		if (!value) {
			throw new RuntimeException("ASSERT FAILED: " + message);
		}
	}

	private static void assertFalse(boolean value, String message) {
		if (value) {
			throw new RuntimeException("ASSERT FAILED: " + message);
		}
	}
}