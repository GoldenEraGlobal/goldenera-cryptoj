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

import java.util.Comparator;

import global.goldenera.cryptoj.common.MiningWindowStateValidation;
import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.serialization.state.miningwindow.MiningWindowStateEncodingStrategy;
import global.goldenera.rlp.RLPOutput;

public class MiningWindowStateV1EncodingStrategy implements MiningWindowStateEncodingStrategy {

	@Override
	public void encode(RLPOutput out, MiningWindowState state) {
		MiningWindowStateValidation.validate(state);
		out.writeLongScalar(state.getWindowSize());
		out.startList();
		state.getOrderedValidatorIdentities().forEach(out::writeBytes);
		out.endList();
		out.startList();
		state.getValidatorBlockCounts().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toHexString()))
				.forEach(entry -> {
					out.startList();
					out.writeBytes(entry.getKey());
					out.writeLongScalar(entry.getValue());
					out.endList();
				});
		out.endList();
		out.writeLongScalar(state.getLastUpdatedBlockHeight());
	}
}
