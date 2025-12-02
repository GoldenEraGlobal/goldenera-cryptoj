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
package global.goldenera.cryptoj.common;

import java.time.Instant;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.payloads.TxPayload;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.Signature;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxType;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.utils.TxUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TxImpl implements Tx {

	TxVersion version;
	Instant timestamp;
	TxType type;
	Network network;
	Long nonce;
	Address recipient;
	Wei amount;
	Address tokenAddress;
	Bytes message;
	Wei fee;
	TxPayload payload;
	Hash referenceHash;
	Signature signature;

	// --- Calculated Fields (Lazy Loaded) ---

	@Getter(lazy = true)
	Address sender = recoverSender();

	@Getter(lazy = true)
	Hash hash = TxUtil.hash(this);

	@Getter(lazy = true)
	int size = TxUtil.size(this);

	private Address recoverSender() {
		Hash hash = TxUtil.hashForSigning(this);
		try {
			return signature.recoverAddress(hash);
		} catch (CryptoJException e) {
			throw new CryptoJFailedException("Failed to recover sender address", e);
		}
	}
}