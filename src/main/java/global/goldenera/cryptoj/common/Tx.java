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

public interface Tx {

	// --- RLP DATA (Stored on Disk/Network) ---

	TxVersion getVersion();

	Instant getTimestamp();

	TxType getType();

	Network getNetwork();

	Long getNonce();

	// Addresses
	Address getRecipient();

	Address getTokenAddress();

	// Economy
	Wei getAmount();

	Wei getFee();

	// Data
	Bytes getMessage();

	TxPayload getPayload();

	Hash getReferenceHash();

	// Security
	Signature getSignature();

	// --- CALCULATED / CACHED DATA (In Memory Only) ---

	/**
	 * Derived from the (v,r,s) signature using ecrecover.
	 * The implementation must cache this value.
	 */
	Address getSender();

	/**
	 * Unique transaction identifier (Hash of RLP data).
	 * The implementation must cache this value.
	 */
	Hash getHash();

	/**
	 * Total RLP size of the transaction in bytes.
	 * The implementation must cache this value.
	 */
	int getSize();
}