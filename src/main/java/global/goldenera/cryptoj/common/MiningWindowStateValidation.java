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

import java.util.LinkedHashMap;
import java.util.Map;

import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;

public final class MiningWindowStateValidation {

	private MiningWindowStateValidation() {
	}

	public static void validate(MiningWindowState state) {
		if (state.getVersion() == null) {
			throw new CryptoJFailedException("MiningWindowState version cannot be null");
		}
		if (state.getWindowSize() == 0 && state.getOrderedValidatorIdentities().isEmpty()
				&& state.getValidatorBlockCounts().isEmpty()
				&& state.getLastUpdatedBlockHeight() == 0) {
			return;
		}
		MiningConsensusRules.validateWindowSize(state.getWindowSize());
		if (state.getOrderedValidatorIdentities().size() > state.getWindowSize()) {
			throw new CryptoJFailedException("Mining window contains more entries than windowSize");
		}
		Map<Address, Long> derivedCounts = new LinkedHashMap<>();
		for (Address address : state.getOrderedValidatorIdentities()) {
			if (address == null) {
				throw new CryptoJFailedException("Mining window cannot contain a null validator identity");
			}
			derivedCounts.merge(address, 1L, Long::sum);
		}
		if (!derivedCounts.equals(state.getValidatorBlockCounts())) {
			throw new CryptoJFailedException("Mining-window count map does not match ordered identities");
		}
	}
}
