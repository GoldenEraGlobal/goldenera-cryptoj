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
package global.goldenera.cryptoj.enums;

import static lombok.AccessLevel.PRIVATE;

import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
public enum TxPayloadType {

	BIP_ADDRESS_ALIAS_ADD(0),
	BIP_ADDRESS_ALIAS_REMOVE(1),
	BIP_AUTHORITY_ADD(2),
	BIP_AUTHORITY_REMOVE(3),
	BIP_NETWORK_PARAMS_SET(4),
	BIP_TOKEN_BURN(5),
	BIP_TOKEN_CREATE(6),
	BIP_TOKEN_MINT(7),
	BIP_TOKEN_UPDATE(8),
	BIP_VOTE(9);

	int code;

	public static TxPayloadType fromCode(int code) {
		for (TxPayloadType type : values()) {
			if (type.getCode() == code) {
				return type;
			}
		}
		throw new CryptoJFailedException("Unknown TxPayloadType code: " + code + " in TxPayloadType.fromCode");
	}
}
