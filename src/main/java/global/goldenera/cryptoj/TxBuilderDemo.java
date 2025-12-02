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

import java.time.Instant;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxType;
import global.goldenera.cryptoj.utils.Amounts;
import global.goldenera.cryptoj.utils.TxUtil;

/**
 * Demonstrates various use cases of TxBuilder for wallet applications.
 */
public class TxBuilderDemo {

	public static void main(String[] args) throws Exception {
		System.out.println("=== TxBuilder Demo for Wallet Applications ===\n");

		// Setup: Generate keys and addresses
		PrivateKey aliceKey = PrivateKey.create();
		Address alice = aliceKey.getAddress();

		PrivateKey bobKey = PrivateKey.create();
		Address bob = bobKey.getAddress();

		System.out.println("Alice: " + alice.toChecksumAddress());
		System.out.println("Bob:   " + bob.toChecksumAddress());
		System.out.println();

		// ==========================================
		// Example 1: Simple Transfer Transaction
		// ==========================================
		System.out.println("--- Example 1: Simple Transfer ---");

		Tx transferTx = TxBuilder.create()
				.type(TxType.TRANSFER)
				.network(Network.TESTNET)
				.sender(alice) // Can use Address directly
				.recipient(bob)
				.amount(Amounts.tokensDecimal("1.5"))
				.fee(Amounts.tokensDecimal("0.001"))
				.nonce(1L)
				.sign(aliceKey); // Sign in one step!

		printTransaction(transferTx, "Simple Transfer");

		// ==========================================
		// Example 2: Transfer with Message
		// ==========================================
		System.out.println("\n--- Example 2: Transfer with Message ---");

		Tx transferWithMessage = TxBuilder.create()
				.type(TxType.TRANSFER)
				.network(Network.TESTNET)
				.sender(aliceKey) // Can use PrivateKey (auto-derives address)
				.recipient(bob)
				.amount(Amounts.tokensDecimal("0.5"))
				.fee(Amounts.tokensDecimal("0.001"))
				.nonce(2L)
				.message("Payment for services") // UTF-8 message
				.sign(aliceKey);

		printTransaction(transferWithMessage, "Transfer with Message");

		// ==========================================
		// Example 3: BIP Token Create
		// ==========================================
		System.out.println("\n--- Example 3: BIP Token Create (NEW FLUENT API) ---");

		Tx tokenCreateTx = TxBuilder.create()
				.tokenCreate()
				.name("MyToken")
				.symbol("mTKN")
				.decimals(18)
				.website("https://mytoken.io")
				.logo("https://mytoken.io/logo.png")
				.maxSupply(java.math.BigInteger.valueOf(1_000_000_000))
				.done()
				.network(Network.MAINNET)
				.sender(alice)
				.nonce(3L)
				.fee(Amounts.tokensDecimal("0.01"))
				.sign(aliceKey);

		printTransaction(tokenCreateTx, "BIP Token Create");

		// ==========================================
		// Example 4: Token Mint
		// ==========================================
		System.out.println("\n--- Example 4: Token Mint (NEW FLUENT API) ---");

		Address tokenAddress = Address.fromHexString("0x1234567890123456789012345678901234567890");

		Tx tokenMintTx = TxBuilder.create()
				.tokenMint()
				.token(tokenAddress)
				.recipient(bob)
				.amount(Amounts.tokens(1000))
				.done()
				.network(Network.MAINNET)
				.sender(alice)
				.nonce(4L)
				.fee(Amounts.tokensDecimal("0.005"))
				.sign(aliceKey);

		printTransaction(tokenMintTx, "Token Mint");

		// ==========================================
		// Example 5: Custom Timestamp
		// ==========================================
		System.out.println("\n--- Example 5: Custom Timestamp ---");

		Instant customTime = Instant.parse("2025-01-01T00:00:00Z");

		Tx scheduledTx = TxBuilder.create()
				.type(TxType.TRANSFER)
				.network(Network.TESTNET)
				.sender(alice)
				.recipient(bob)
				.amount(Amounts.tokensDecimal("0.1"))
				.fee(Amounts.tokensDecimal("0.001"))
				.nonce(5L)
				.timestamp(customTime) // Custom timestamp
				.sign(aliceKey);

		printTransaction(scheduledTx, "Scheduled Transfer");

		// ==========================================
		// Example 6: Preview Hash Before Signing
		// ==========================================
		System.out.println("\n--- Example 6: Preview Hash Before Signing ---");

		TxBuilder previewBuilder = TxBuilder.create()
				.type(TxType.TRANSFER)
				.network(Network.TESTNET)
				.sender(alice)
				.recipient(bob)
				.amount(Amounts.tokens(2))
				.fee(Amounts.tokensDecimal("0.001"))
				.nonce(6L);

		// Preview what will be signed
		Hash previewHash = previewBuilder.computeHash();
		System.out.println("  Hash to sign: " + previewHash.toHexString());

		// Estimate size
		int estimatedSize = previewBuilder.estimateSize();
		System.out.println("  Estimated size: " + estimatedSize + " bytes");

		// Now sign
		Tx previewedTx = previewBuilder.sign(aliceKey);
		Hash previewedTxId = TxUtil.hash(previewedTx);
		System.out.println("  Transaction signed successfully! ID: " + previewedTxId.toHexString());

		// ==========================================
		// Example 7: Build Unsigned (for Hardware Wallets)
		// ==========================================
		System.out.println("\n--- Example 7: Build Unsigned (for Hardware Wallets) ---");

		Tx unsignedTx = TxBuilder.create()
				.type(TxType.TRANSFER)
				.network(Network.MAINNET)
				.sender(alice)
				.recipient(bob)
				.amount(Amounts.tokens(5))
				.fee(Amounts.tokensDecimal("0.002"))
				.nonce(7L)
				.buildUnsigned();

		System.out.println("  Unsigned transaction created.");
		System.out.println("  Sender: " + unsignedTx.getSender().toChecksumAddress());
		System.out.println("  Recipient: " + unsignedTx.getRecipient().toChecksumAddress());
		System.out.println("  Amount: " + unsignedTx.getAmount() + " Wei");
		System.out.println("  Signature: " + (unsignedTx.getSignature() == null ? "null (unsigned)" : "present"));

		// Hash for hardware wallet to sign
		Hash hashForHardwareWallet = TxUtil.hashForSigning(unsignedTx);
		System.out.println("  Hash for hardware wallet: " + hashForHardwareWallet.toHexString());

		System.out.println("\n=== Demo Complete ===");
	}

	/**
	 * Helper method to print transaction details.
	 */
	private static void printTransaction(Tx tx, String label) {
		System.out.println("  Transaction: " + label);
		System.out.println("  Type: " + tx.getType());
		System.out.println("  Network: " + tx.getNetwork());
		System.out.println("  Sender: " + tx.getSender().toChecksumAddress());
		if (tx.getRecipient() != null) {
			System.out.println("  Recipient: " + tx.getRecipient().toChecksumAddress());
		}
		System.out.println("  Amount: " + tx.getAmount() + " Wei");
		System.out.println("  Fee: " + tx.getFee() + " Wei");
		System.out.println("  Nonce: " + tx.getNonce());
		System.out.println("  Timestamp: " + tx.getTimestamp());

		// Verify signature
		if (tx.getSignature() != null) {
			Hash txHash = TxUtil.hashForSigning(tx);
			boolean isValid = tx.getSignature().validate(txHash, tx.getSender());
			System.out.println("  Signature: " + (isValid ? "✓ Valid" : "✗ Invalid"));

			// Show transaction ID (with signature)
			Hash txId = TxUtil.hash(tx);
			System.out.println("  Transaction ID: " + txId.toHexString());
		} else {
			System.out.println("  Signature: (unsigned)");
		}
	}
}
