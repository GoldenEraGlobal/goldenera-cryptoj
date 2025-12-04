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
package global.goldenera.cryptoj.utils;

import java.math.BigInteger;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import lombok.experimental.UtilityClass;

/**
 * Utility class for working with Wei amounts in GoldenEra blockchain.
 * 
 * <p>
 * Provides convenient factory methods for creating Wei values without
 * Ethereum-specific terminology.
 * 
 * <p>
 * GoldenEra uses 8 decimal places for native tokens (like Bitcoin),
 * but supports up to 18 decimals for custom tokens.
 * 
 * @author GoldenEra CryptoJ Team
 */
@UtilityClass
public class Amounts {

	/**
	 * Standard number of decimals for native token
	 */
	public static final int STANDARD_DECIMALS = 9;

	/**
	 * Maximum number of decimals supported (18).
	 */
	public static final int MAX_DECIMALS = 18;

	/**
	 * Wei per whole native token (1 token = 10^8 wei).
	 */
	public static final BigInteger WEI_PER_TOKEN = BigInteger.TEN.pow(STANDARD_DECIMALS); // 100_000_000

	/**
	 * Creates a Wei amount from whole tokens (8 decimals).
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Wei amount = Amounts.tokens(100); // 100 tokens
	 * Wei fee = Amounts.tokens(1); // 1 token
	 * }</pre>
	 * 
	 * @param tokens number of whole tokens
	 * @return Wei amount
	 */
	public static Wei tokens(long tokens) {
		return Wei.valueOf(BigInteger.valueOf(tokens).multiply(WEI_PER_TOKEN));
	}

	/**
	 * Creates a Wei amount from whole tokens (8 decimals).
	 * 
	 * @param tokens number of whole tokens
	 * @return Wei amount
	 */
	public static Wei tokens(BigInteger tokens) {
		return Wei.valueOf(tokens.multiply(WEI_PER_TOKEN));
	}

	/**
	 * Creates a Wei amount from wei units.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Wei amount = Amounts.wei(10000000L); // 0.1 tokens (8 decimals)
	 * }</pre>
	 * 
	 * @param wei amount in wei
	 * @return Wei amount
	 */
	public static Wei wei(long wei) {
		return Wei.valueOf(BigInteger.valueOf(wei));
	}

	/**
	 * Creates a Wei amount from wei units.
	 * 
	 * @param wei amount in wei
	 * @return Wei amount
	 */
	public static Wei wei(BigInteger wei) {
		return Wei.valueOf(wei);
	}

	/**
	 * Creates a Wei amount from tokens with decimal precision (8 decimals default).
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Wei amount = Amounts.tokensDecimal("1.5"); // 1.5 tokens
	 * Wei fee = Amounts.tokensDecimal("0.001"); // 0.001 tokens
	 * Wei precise = Amounts.tokensDecimal("123.45678901"); // 123.45678901 tokens
	 * }</pre>
	 * 
	 * @param tokensDecimal token amount as decimal string (e.g., "1.5", "0.001")
	 * @return Wei amount
	 */
	public static Wei tokensDecimal(String tokensDecimal) {
		return tokensWithDecimals(tokensDecimal, STANDARD_DECIMALS);
	}

	/**
	 * Creates a Wei amount from tokens with custom decimals.
	 * 
	 * <p>
	 * Useful for custom tokens with different decimal places.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * // Custom token with 6 decimals
	 * Wei custom6 = Amounts.tokensWithDecimals("100.50", 6);
	 * 
	 * // Custom token with 18 decimals
	 * Wei custom18 = Amounts.tokensWithDecimals("50.123456", 18);
	 * 
	 * // Using constant
	 * Wei standard = Amounts.tokensWithDecimals("100.5", Decimals.STANDARD);
	 * }</pre>
	 * 
	 * @param tokensDecimal token amount as decimal string
	 * @param decimals      number of decimal places for the token (0-18)
	 * @return Wei amount
	 */
	public static Wei tokensWithDecimals(String tokensDecimal, int decimals) {
		if (decimals < 0 || decimals > MAX_DECIMALS) {
			throw new CryptoJFailedException(
					String.format("Decimals must be between 0 and %d, got: %d", MAX_DECIMALS, decimals));
		}

		BigInteger weiAmount = new BigInteger(tokensDecimal.replace(".", ""));
		int decimalPlaces = tokensDecimal.contains(".")
				? tokensDecimal.length() - tokensDecimal.indexOf('.') - 1
				: 0;

		// Scale to specified decimals
		int scaleFactor = decimals - decimalPlaces;
		if (scaleFactor > 0) {
			weiAmount = weiAmount.multiply(BigInteger.TEN.pow(scaleFactor));
		} else if (scaleFactor < 0) {
			weiAmount = weiAmount.divide(BigInteger.TEN.pow(-scaleFactor));
		}

		return Wei.valueOf(weiAmount);
	}

	/**
	 * Zero amount.
	 */
	public static Wei zero() {
		return Wei.ZERO;
	}

	/**
	 * Common token decimal constants.
	 */
	public static class Decimals {
		/** Standard token decimals */
		public static final int STANDARD = 9;

		/** Maximum supported decimals */
		public static final int MAX = 18;
	}
}
