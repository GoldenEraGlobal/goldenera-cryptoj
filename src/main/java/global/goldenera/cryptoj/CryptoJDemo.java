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

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.datatypes.Signature;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoJDemo {

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	public static void main(String[] args) throws Exception {

		// === 1. Mnemonic and Key Generation ===
		String mnemonic = PrivateKey.generateMnemonic();
		String password = "myStrongPassword";

		// Derive the first account (index 0)
		PrivateKey keyPair_Account1 = PrivateKey.load(mnemonic, password, 0);

		// Derive the second account (index 1)
		PrivateKey keyPair_Account2 = PrivateKey.load(mnemonic, password, 1);

		// === 2. Address Derivation ===
		Address address1 = Address.fromKeyPair(keyPair_Account1);
		Address address2 = Address.fromKeyPair(keyPair_Account2);

		// Encode for display (EIP-55 Checksum)
		System.out.println("Account 1: " + address1.toChecksumAddress());
		System.out.println("Account 2: " + address2.toChecksumAddress());

		// === 3. Signing a Message (Transaction) ===
		// (This would be the RLP-encoded bytes of your transaction)
		Bytes transactionData = Bytes.wrap("Test Data".getBytes());

		// 3a. Hashing (Keccak-256)
		Hash txHash = Hash.hash(transactionData);
		System.out.println("\nHash to sign: " + txHash.toHexString());

		// 3b. Signing
		Signature signature = keyPair_Account1.sign(txHash);

		// === 4. Verifying the Signature (Node-side) ===
		System.out.println("Verifying signature...");

		// 4a. Recover the address from the signature
		Address recoveredAddress = signature.recoverAddress(txHash);

		// 4b. Validate
		boolean isValid = signature.validate(txHash, address1);

		if (isValid && address1.equals(recoveredAddress)) {
			System.out.println(
					"RESULT: Signature is valid. Sender: " + recoveredAddress.toChecksumAddress());
		} else {
			System.out.println("RESULT: Signature is INVALID.");
		}
	}
}