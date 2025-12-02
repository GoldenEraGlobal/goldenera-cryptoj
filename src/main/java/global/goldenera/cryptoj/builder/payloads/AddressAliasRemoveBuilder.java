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

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.payloads.bip.TxBipAddressAliasRemovePayloadImpl;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for Address Alias Remove payloads.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.removeAddressAlias()
 * 		.alias("myalias")
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(myAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class AddressAliasRemoveBuilder {

	private final TxBuilder parent;
	private String alias;

	public AddressAliasRemoveBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	/**
	 * Sets the alias to remove.
	 * 
	 * @param alias the alias to remove
	 * @return this builder for chaining
	 */
	public AddressAliasRemoveBuilder alias(@NonNull String alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * Completes the address alias remove payload configuration and returns to the
	 * parent builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipAddressAliasRemovePayloadImpl payload = TxBipAddressAliasRemovePayloadImpl.builder()
				.alias(alias)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
