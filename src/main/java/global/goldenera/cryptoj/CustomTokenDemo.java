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

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.utils.Amounts;

/**
 * Demonstrates custom token transfers with different decimals.
 */
public class CustomTokenDemo {

	public static void main(String[] args) throws Exception {
		System.out.println("=== Custom Token Transfer Demo ===\n");

		// Setup
		PrivateKey senderKey = PrivateKey.create();
		Address sender = senderKey.getAddress();
		Address recipient = Address.fromHexString("0x1234567890123456789012345678901234567890");
		Address usdcTokenAddress = Address.fromHexString("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48");
		Address wbtcTokenAddress = Address.fromHexString("0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599");

		System.out.println("Sender: " + sender.toChecksumAddress());
		System.out.println("Recipient: " + recipient.toChecksumAddress());
		System.out.println();

		// ==========================================
		// Example 1: Native Transfer (8 decimals)
		// ==========================================
		System.out.println("--- Example 1: Native Transfer (8 decimals) ---");

		Tx nativeTransfer = TxBuilder.create()
				.tokenMint()
				.token(Address.NATIVE_TOKEN)
				.recipient(recipient)
				.amount(Amounts.tokensDecimal("1"))
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(1L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("  Token: Native");
		System.out.println("  Decimals: 8");
		System.out.println("  Amount: 1 tokens");
		System.out.println("  Size value: " + nativeTransfer.getSize());
		System.out.println();

		// ==========================================
		// Example 1: USDC Transfer (6 decimals)
		// ==========================================
		System.out.println("--- Example 1: USDC Transfer (6 decimals) ---");

		Tx usdcTransfer = TxBuilder.create()
				.tokenMint()
				.token(usdcTokenAddress)
				.recipient(recipient)
				.amount(Amounts.tokensWithDecimals("100.50", 6)) // 100.50 USDC
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(1L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("  Token: USDC");
		System.out.println("  Decimals: 6");
		System.out.println("  Amount: 100.50 USDC");
		System.out.println("  Wei value: " + usdcTransfer.getPayload());
		System.out.println("  Size value: " + usdcTransfer.getSize());
		System.out.println();

		// Using constant
		Tx usdcTransfer2 = TxBuilder.create()
				.tokenMint()
				.token(usdcTokenAddress)
				.recipient(recipient)
				.amount(Amounts.tokensWithDecimals("250.75", 6)) // Using constant
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(2L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("--- Example 1b: USDC with Constant ---");
		System.out.println("  Amount: 250.75 USDC (using 6)");
		System.out.println();

		// ==========================================
		// Example 2: WBTC Transfer (8 decimals)
		// ==========================================
		System.out.println("--- Example 2: WBTC Transfer (8 decimals) ---");

		Tx wbtcTransfer = TxBuilder.create()
				.tokenMint()
				.token(wbtcTokenAddress)
				.recipient(recipient)
				.amount(Amounts.tokensWithDecimals("0.5", 8)) // 0.5 WBTC
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(3L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("  Token: WBTC");
		System.out.println("  Decimals: 8");
		System.out.println("  Amount: 0.5 WBTC");
		System.out.println();

		// ==========================================
		// Example 3: DAI Transfer (18 decimals - standard)
		// ==========================================
		System.out.println("--- Example 3: DAI Transfer (18 decimals - standard) ---");

		Tx daiTransfer = TxBuilder.create()
				.tokenMint()
				.token(Address.fromHexString("0x6B175474E89094C44Da98b954EedeAC495271d0F"))
				.recipient(recipient)
				// For 18 decimals, can use either:
				.amount(Amounts.tokensDecimal("1000.123456")) // tokensDecimal (defaults to 18)
				// OR: .amount(Amounts.tokensWithDecimals("1000.123456", 18))
				// OR: .amount(Amounts.tokensWithDecimals("1000.123456", Amounts.Decimals.DAI))
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(4L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("  Token: DAI");
		System.out.println("  Decimals: 18 (standard)");
		System.out.println("  Amount: 1000.123456 DAI");
		System.out.println("  Note: For 18 decimals, tokensDecimal() is simpler!");
		System.out.println();

		// ==========================================
		// Example 4: Custom Token (e.g., 9 decimals)
		// ==========================================
		System.out.println("--- Example 4: Custom Token (9 decimals) ---");

		Tx customTokenTransfer = TxBuilder.create()
				.tokenMint()
				.token(Address.fromHexString("0xabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"))
				.recipient(recipient)
				.amount(Amounts.tokensWithDecimals("500.12345", 9)) // 9 decimals
				.done()
				.network(Network.MAINNET)
				.sender(sender)
				.nonce(5L)
				.fee(Amounts.tokensDecimal("0.001"))
				.sign(senderKey);

		System.out.println("  Token: CustomToken");
		System.out.println("  Decimals: 9");
		System.out.println("  Amount: 500.12345 tokens");
		System.out.println();

		// ==========================================
		// Comparison Table
		// ==========================================
		System.out.println("--- Decimal Comparison ---");
		System.out.println("Token  | Decimals | Amount     | Wei Value");
		System.out.println("-------|----------|------------|------------------");
		System.out.println("USDC   | 6        | 100.50     | " + Amounts.tokensWithDecimals("100.50", 6));
		System.out.println("USDT   | 6        | 100.50     | " + Amounts.tokensWithDecimals("100.50", 6));
		System.out.println("WBTC   | 8        | 0.5        | " + Amounts.tokensWithDecimals("0.5", 8));
		System.out.println("DAI    | 18       | 1.0        | " + Amounts.tokensDecimal("1.0"));
		System.out.println("Native | 8        | 1.0        | " + Amounts.tokensDecimal("1.0"));
		System.out.println();

		System.out.println("=== Demo Complete ===");
	}
}
