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

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenCreatePayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxType;
import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.utils.Amounts;

/**
 * Demonstrates that BIP_CREATE and BIP_VOTE transactions require null amount.
 */
public class BipAmountValidationDemo {

	public static void main(String[] args) {
		System.out.println("=== BIP Amount Validation Demo ===\n");

		try {
			PrivateKey key = PrivateKey.create();
			Address sender = key.getAddress();
			Address recipient = Address.fromHexString("0x1234567890123456789012345678901234567890");
			Address tokenAddress = Address.NATIVE_TOKEN;

			// ==========================================
			// Test 1: BIP_CREATE with null amount - SHOULD SUCCEED
			// ==========================================
			System.out.println("--- Test 1: BIP_CREATE with null amount (via tokenMint) ---");
			try {
				Tx tokenMintTx = TxBuilder.create()
						.tokenMint()
						.token(tokenAddress)
						.recipient(recipient)
						.amount(Amounts.tokens(1000))
						.done()
						.network(Network.MAINNET)
						.nonce(1L)
						.fee(Amounts.tokensDecimal("0.005"))
						.sign(key);

				System.out.println("✅ SUCCESS: BIP_CREATE with null amount at transaction level");
				System.out.println("   Transaction amount: " + tokenMintTx.getAmount());
				System.out.println("   Payload amount: "
						+ ((global.goldenera.cryptoj.common.payloads.bip.TxBipTokenMintPayload) tokenMintTx
								.getPayload()).getAmount());
				System.out.println();
			} catch (CryptoJException e) {
				System.out.println("❌ FAILED: " + e.getMessage());
				System.out.println();
			}

			// ==========================================
			// Test 2: BIP_CREATE with non-null amount - SHOULD FAIL
			// ==========================================
			System.out.println("--- Test 2: BIP_CREATE with non-null amount - SHOULD FAIL ---");
			try {
				var payload = TxBipTokenCreatePayloadImpl.builder()
						.name("TestToken")
						.smallestUnitName("TST")
						.numberOfDecimals(18)
						.build();

				Tx tx = TxBuilder.create()
						.type(TxType.BIP_CREATE)
						.network(Network.MAINNET)
						.nonce(2L)
						.fee(Amounts.tokensDecimal("0.005"))
						.payload(payload)
						.amount(Wei.fromEth(1)) // This should cause validation error
						.sign(key);

				System.out.println("❌ FAILED: Should have thrown exception!");
				System.out.println();
			} catch (CryptoJException e) {
				System.out.println("✅ SUCCESS: Correctly rejected non-null amount");
				System.out.println("   Error: " + e.getMessage());
				System.out.println();
			}

			// ==========================================
			// Test 3: BIP_VOTE with null amount - SHOULD SUCCEED
			// ==========================================
			System.out.println("--- Test 3: BIP_VOTE with null amount - SHOULD SUCCEED ---");
			try {
				var payload = TxBipTokenCreatePayloadImpl.builder()
						.name("VotePayload")
						.smallestUnitName("VTE")
						.numberOfDecimals(18)
						.build();

				Hash referenceHash = Hash
						.fromHexString("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");

				Tx tx = TxBuilder.create()
						.type(TxType.BIP_VOTE)
						.network(Network.MAINNET)
						.nonce(3L)
						.fee(Amounts.tokensDecimal("0.005"))
						.payload(payload)
						.referenceHash(referenceHash)
						// amount is null by default
						.sign(key);

				System.out.println("✅ SUCCESS: BIP_VOTE with null amount");
				System.out.println("   Transaction amount: " + tx.getAmount());
				System.out.println();
			} catch (CryptoJException e) {
				System.out.println("❌ FAILED: " + e.getMessage());
				System.out.println();
			}

			// ==========================================
			// Test 4: BIP_VOTE with non-null amount - SHOULD FAIL
			// ==========================================
			System.out.println("--- Test 4: BIP_VOTE with non-null amount - SHOULD FAIL ---");
			try {
				var payload = TxBipTokenCreatePayloadImpl.builder()
						.name("VotePayload")
						.smallestUnitName("VTE")
						.numberOfDecimals(18)
						.build();

				Hash referenceHash = Hash
						.fromHexString("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");

				Tx tx = TxBuilder.create()
						.type(TxType.BIP_VOTE)
						.network(Network.MAINNET)
						.nonce(4L)
						.fee(Amounts.tokensDecimal("0.005"))
						.payload(payload)
						.referenceHash(referenceHash)
						.amount(Wei.fromEth(1)) // This should cause validation error
						.sign(key);

				System.out.println("❌ FAILED: Should have thrown exception!");
				System.out.println();
			} catch (CryptoJException e) {
				System.out.println("✅ SUCCESS: Correctly rejected non-null amount");
				System.out.println("   Error: " + e.getMessage());
				System.out.println();
			}

			// ==========================================
			// Test 5: TRANSFER with amount - SHOULD SUCCEED
			// ==========================================
			System.out.println("--- Test 5: TRANSFER with amount - SHOULD SUCCEED ---");
			try {
				Tx tx = TxBuilder.create()
						.type(TxType.TRANSFER)
						.network(Network.MAINNET)
						.recipient(recipient)
						.amount(Wei.fromEth(1))
						.nonce(5L)
						.fee(Amounts.tokensDecimal("0.005"))
						.sign(key);

				System.out.println("✅ SUCCESS: TRANSFER with amount works fine");
				System.out.println("   Transaction amount: " + tx.getAmount());
				System.out.println();
			} catch (CryptoJException e) {
				System.out.println("❌ FAILED: " + e.getMessage());
				System.out.println();
			}

			System.out.println("=== All Tests Complete ===");

		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
