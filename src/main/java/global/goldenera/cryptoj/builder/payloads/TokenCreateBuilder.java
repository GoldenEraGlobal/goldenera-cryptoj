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
package global.goldenera.cryptoj.builder.payloads;

import java.math.BigInteger;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenCreatePayloadImpl;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for Token Create payloads.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.tokenCreate()
 * 		.name("MyToken")
 * 		.symbol("MTK")
 * 		.decimals(18)
 * 		.website("https://mytoken.io")
 * 		.logo("https://mytoken.io/logo.png")
 * 		.maxSupply(BigInteger.valueOf(1_000_000_000))
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(myAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class TokenCreateBuilder {

	private final TxBuilder parent;
	private String name;
	private String smallestUnitName;
	private int numberOfDecimals;
	private String websiteUrl;
	private String logoUrl;
	private BigInteger maxSupply;

	public TokenCreateBuilder(TxBuilder parent) {
		this.parent = parent;
		this.numberOfDecimals = 18; // Default to 18 decimals (like ETH)
	}

	/**
	 * Sets the token name.
	 * 
	 * @param name token name (e.g., "MyToken")
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder name(@NonNull String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the smallest unit name (symbol).
	 * 
	 * @param symbol token symbol (e.g., "MTK")
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder symbol(@NonNull String symbol) {
		this.smallestUnitName = symbol;
		return this;
	}

	/**
	 * Sets the number of decimal places.
	 * Default: 18 (like ETH)
	 * 
	 * @param decimals number of decimals (0-18 typically)
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder decimals(int decimals) {
		this.numberOfDecimals = decimals;
		return this;
	}

	/**
	 * Sets the token website URL (optional).
	 * 
	 * @param websiteUrl website URL
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder website(String websiteUrl) {
		this.websiteUrl = websiteUrl;
		return this;
	}

	/**
	 * Sets the token logo URL (optional).
	 * 
	 * @param logoUrl logo URL
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder logo(String logoUrl) {
		this.logoUrl = logoUrl;
		return this;
	}

	/**
	 * Sets the maximum supply (optional).
	 * If not set, supply will be unlimited.
	 * 
	 * @param maxSupply maximum supply
	 * @return this builder for chaining
	 */
	public TokenCreateBuilder maxSupply(BigInteger maxSupply) {
		this.maxSupply = maxSupply;
		return this;
	}

	/**
	 * Completes the token create payload configuration and returns to the parent
	 * builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipTokenCreatePayloadImpl payload = TxBipTokenCreatePayloadImpl.builder()
				.name(name)
				.smallestUnitName(smallestUnitName)
				.numberOfDecimals(numberOfDecimals)
				.websiteUrl(websiteUrl)
				.logoUrl(logoUrl)
				.maxSupply(maxSupply)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
