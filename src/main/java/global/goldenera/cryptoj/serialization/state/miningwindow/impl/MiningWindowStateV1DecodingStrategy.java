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
package global.goldenera.cryptoj.serialization.state.miningwindow.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import global.goldenera.cryptoj.common.MiningWindowStateValidation;
import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.common.state.impl.MiningWindowStateImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.state.MiningWindowStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.miningwindow.MiningWindowStateDecodingStrategy;
import global.goldenera.rlp.RLPInput;

public class MiningWindowStateV1DecodingStrategy implements MiningWindowStateDecodingStrategy {

	@Override
	public MiningWindowState decode(RLPInput input) {
		long windowSize = input.readLongScalar();
		int identityCount = input.enterList();
		List<Address> identities = new ArrayList<>(identityCount);
		for (int i = 0; i < identityCount; i++) {
			identities.add(Address.wrap(input.readBytes()));
		}
		input.leaveList();

		int countEntries = input.enterList();
		Map<Address, Long> counts = new LinkedHashMap<>();
		String previousAddress = null;
		for (int i = 0; i < countEntries; i++) {
			if (input.enterList() != 2) {
				throw new CryptoJFailedException("Mining-window count entry must have exactly two fields");
			}
			Address address = Address.wrap(input.readBytes());
			String addressHex = address.toHexString();
			if (previousAddress != null && previousAddress.compareTo(addressHex) >= 0) {
				throw new CryptoJFailedException("Mining-window count entries must use canonical address order");
			}
			long count = input.readLongScalar();
			if (count < 1 || counts.put(address, count) != null) {
				throw new CryptoJFailedException("Mining-window counts must be positive and unique");
			}
			previousAddress = addressHex;
			input.leaveList();
		}
		input.leaveList();
		long lastUpdatedBlockHeight = input.readLongScalar();
		MiningWindowState state = new MiningWindowStateImpl(MiningWindowStateVersion.V1, windowSize, identities,
				counts, lastUpdatedBlockHeight);
		MiningWindowStateValidation.validate(state);
		return state;
	}
}
