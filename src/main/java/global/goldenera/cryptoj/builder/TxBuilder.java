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
package global.goldenera.cryptoj.builder;

import java.time.Instant;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.builder.payloads.AddressAliasAddBuilder;
import global.goldenera.cryptoj.builder.payloads.AddressAliasRemoveBuilder;
import global.goldenera.cryptoj.builder.payloads.AuthorityAddBuilder;
import global.goldenera.cryptoj.builder.payloads.AuthorityRemoveBuilder;
import global.goldenera.cryptoj.builder.payloads.BipVoteBuilder;
import global.goldenera.cryptoj.builder.payloads.NetworkParamsBuilder;
import global.goldenera.cryptoj.builder.payloads.TokenBurnBuilder;
import global.goldenera.cryptoj.builder.payloads.TokenCreateBuilder;
import global.goldenera.cryptoj.builder.payloads.TokenMintBuilder;
import global.goldenera.cryptoj.builder.payloads.TokenUpdateBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.common.TxImpl;
import global.goldenera.cryptoj.common.payloads.TxPayload;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.datatypes.Signature;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxType;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.utils.TxUtil;
import lombok.NonNull;

/**
 * User-friendly Transaction Builder for wallet applications.
 * 
 * <p>
 * Provides a fluent API for building and signing transactions with sensible
 * defaults
 * and compile-time safety.
 * 
 * <h2>Basic Usage:</h2>
 * 
 * <pre>{@code
 * // Simple transfer
 * Tx tx = TxBuilder.create()
 * 		.type(TxType.TRANSFER)
 * 		.network(Network.MAINNET)
 * 		.sender(myAddress)
 * 		.recipient(recipientAddress)
 * 		.amount(Wei.fromEth(1))
 * 		.fee(Wei.fromEth(0.001))
 * 		.nonce(1L)
 * 		.sign(myPrivateKey);
 * 
 * // BIP transaction with payload
 * Tx bipTx = TxBuilder.create()
 * 		.type(TxType.BIP_CREATE)
 * 		.network(Network.TESTNET)
 * 		.sender(myAddress)
 * 		.fee(Wei.fromEth(0.01))
 * 		.nonce(2L)
 * 		.payload(tokenMintPayload)
 * 		.sign(myPrivateKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 * @since 1.0
 */
public class TxBuilder {

	// Core transaction fields
	private TxVersion version;
	private Instant timestamp;
	private TxType type;
	private Network network;
	private Address sender;
	private Long nonce;
	private Address recipient;
	private Wei amount;
	private Address tokenAddress;
	private Bytes message;
	private Wei fee;
	private TxPayload payload;
	private Hash referenceHash;

	/**
	 * Private constructor - use {@link #create()} to instantiate.
	 */
	private TxBuilder() {
		// Set sensible defaults
		this.version = TxVersion.V1;
		this.timestamp = Instant.now();
		this.amount = null; // Null by default - BIP_CREATE/BIP_VOTE require null
		this.fee = Wei.ZERO;
		this.message = null;
	}

	/**
	 * Creates a new transaction builder with default values.
	 * 
	 * @return new TxBuilder instance
	 */
	public static TxBuilder create() {
		return new TxBuilder();
	}

	/**
	 * Sets the transaction version.
	 * Default: {@link TxVersion#V1}
	 * 
	 * @param version transaction version
	 * @return this builder for chaining
	 */
	public TxBuilder version(@NonNull TxVersion version) {
		this.version = version;
		return this;
	}

	/**
	 * Sets the transaction timestamp.
	 * Default: current time ({@link Instant#now()})
	 * 
	 * @param timestamp transaction timestamp
	 * @return this builder for chaining
	 */
	public TxBuilder timestamp(@NonNull Instant timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	/**
	 * Sets the transaction type.
	 * Required field.
	 * 
	 * @param type transaction type (TRANSFER, BIP_CREATE, etc.)
	 * @return this builder for chaining
	 */
	public TxBuilder type(@NonNull TxType type) {
		this.type = type;
		if (type == TxType.TRANSFER && tokenAddress == null) {
			tokenAddress = Address.NATIVE_TOKEN;
		}
		return this;
	}

	/**
	 * Sets the network (MAINNET or TESTNET).
	 * Required field.
	 * 
	 * @param network target network
	 * @return this builder for chaining
	 */
	public TxBuilder network(@NonNull Network network) {
		this.network = network;
		return this;
	}

	/**
	 * Sets the sender address.
	 * Required field.
	 * 
	 * @param sender sender's address
	 * @return this builder for chaining
	 */
	public TxBuilder sender(@NonNull Address sender) {
		this.sender = sender;
		return this;
	}

	/**
	 * Sets the sender using a private key (derives the address automatically).
	 * 
	 * @param privateKey sender's private key
	 * @return this builder for chaining
	 */
	public TxBuilder sender(@NonNull PrivateKey privateKey) {
		this.sender = privateKey.getAddress();
		return this;
	}

	/**
	 * Sets the transaction nonce (account sequence number).
	 * Required for most transaction types.
	 * 
	 * @param nonce account nonce
	 * @return this builder for chaining
	 */
	public TxBuilder nonce(long nonce) {
		this.nonce = nonce;
		return this;
	}

	/**
	 * Sets the recipient address.
	 * Required for TRANSFER transactions.
	 * 
	 * @param recipient recipient's address
	 * @return this builder for chaining
	 */
	public TxBuilder recipient(Address recipient) {
		this.recipient = recipient;
		return this;
	}

	/**
	 * Sets the amount to transfer.
	 * Default: null
	 * 
	 * <p>
	 * Note: BIP_CREATE and BIP_VOTE transactions must have null amount.
	 * For TOKEN_MINT/BURN operations, use the amount in the payload instead.
	 * 
	 * @param amount amount in Wei
	 * @return this builder for chaining
	 */
	public TxBuilder amount(@NonNull Wei amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Sets the token address (for token transfers).
	 * 
	 * @param tokenAddress token contract address
	 * @return this builder for chaining
	 */
	public TxBuilder tokenAddress(Address tokenAddress) {
		this.tokenAddress = tokenAddress;
		if (tokenAddress == null) {
			this.tokenAddress = Address.NATIVE_TOKEN;
		}
		return this;
	}

	/**
	 * Sets the message field (arbitrary data).
	 * Default: empty bytes
	 * 
	 * @param message message bytes
	 * @return this builder for chaining
	 */
	public TxBuilder message(@NonNull Bytes message) {
		this.message = message;
		return this;
	}

	/**
	 * Sets the message field from a UTF-8 string.
	 * 
	 * @param message message string
	 * @return this builder for chaining
	 */
	public TxBuilder message(@NonNull String message) {
		this.message = Bytes.wrap(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		return this;
	}

	/**
	 * Sets the transaction fee.
	 * Default: {@link Wei#ZERO}
	 * 
	 * @param fee transaction fee in Wei
	 * @return this builder for chaining
	 */
	public TxBuilder fee(@NonNull Wei fee) {
		this.fee = fee;
		return this;
	}

	/**
	 * Sets the BIP payload (for BIP_CREATE and BIP_VOTE transactions).
	 * 
	 * @param payload BIP transaction payload
	 * @return this builder for chaining
	 */
	public TxBuilder payload(TxPayload payload) {
		this.payload = payload;
		return this;
	}

	/**
	 * Sets the reference hash (for linking transactions).
	 * 
	 * @param referenceHash hash of referenced transaction
	 * @return this builder for chaining
	 */
	public TxBuilder referenceHash(Hash referenceHash) {
		this.referenceHash = referenceHash;
		return this;
	}

	// ==========================================
	// Fluent Payload Builders
	// ==========================================

	/**
	 * Starts building a Token Mint transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.tokenMint()
	 * 		.token(tokenAddress)
	 * 		.recipient(recipientAddress)
	 * 		.amount(Wei.fromEth(1000))
	 * 		.minerFee(Wei.fromEth(0.001))
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(myAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return TokenMintBuilder for configuring the mint payload
	 */
	public TokenMintBuilder tokenMint() {
		return new TokenMintBuilder(this);
	}

	/**
	 * Starts building a Token Create transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.tokenCreate()
	 * 		.name("MyToken")
	 * 		.symbol("MTK")
	 * 		.decimals(18)
	 * 		.website("https://mytoken.io")
	 * 		.maxSupply(BigInteger.valueOf(1_000_000_000))
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(myAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return TokenCreateBuilder for configuring the token
	 */
	public TokenCreateBuilder tokenCreate() {
		return new TokenCreateBuilder(this);
	}

	/**
	 * Starts building a Token Burn transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.tokenBurn()
	 * 		.token(tokenAddress)
	 * 		.from(holderAddress)
	 * 		.amount(Wei.fromEth(100))
	 * 		.minerFee(Wei.fromEth(0.001))
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(myAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return TokenBurnBuilder for configuring the burn
	 */
	public TokenBurnBuilder tokenBurn() {
		return new TokenBurnBuilder(this);
	}

	/**
	 * Starts building a BIP Vote transaction.
	 * 
	 * <p>
	 * Example - Approve:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.vote()
	 * 		.approve(bipProposalHash)
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(authorityAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return BipVoteBuilder for voting on proposals
	 */
	public BipVoteBuilder vote() {
		return new BipVoteBuilder(this);
	}

	/**
	 * Starts building a Token Update transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.tokenUpdate()
	 * 		.token(tokenAddress)
	 * 		.name("NewName")
	 * 		.symbol("NEW")
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(ownerAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return TokenUpdateBuilder for updating token metadata
	 */
	public TokenUpdateBuilder tokenUpdate() {
		return new TokenUpdateBuilder(this);
	}

	/**
	 * Starts building an Add Authority transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.addAuthority()
	 * 		.authority(newAuthorityAddress)
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(currentAuthorityAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return AuthorityAddBuilder for adding authorities
	 */
	public AuthorityAddBuilder addAuthority() {
		return new AuthorityAddBuilder(this);
	}

	/**
	 * Starts building a Remove Authority transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.removeAuthority()
	 * 		.authority(authorityToRemove)
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(currentAuthorityAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return AuthorityRemoveBuilder for removing authorities
	 */
	public AuthorityRemoveBuilder removeAuthority() {
		return new AuthorityRemoveBuilder(this);
	}

	/**
	 * Starts building an Add Address Alias transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.addAddressAlias()
	 * 		.address(myAddress)
	 * 		.alias("myusername")
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(myAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return AddressAliasAddBuilder for adding aliases
	 */
	public AddressAliasAddBuilder addAddressAlias() {
		return new AddressAliasAddBuilder(this);
	}

	/**
	 * Starts building a Remove Address Alias transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.removeAddressAlias()
	 * 		.alias("oldusername")
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(myAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return AddressAliasRemoveBuilder for removing aliases
	 */
	public AddressAliasRemoveBuilder removeAddressAlias() {
		return new AddressAliasRemoveBuilder(this);
	}

	/**
	 * Starts building a Set Network Parameters transaction.
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * Tx tx = TxBuilder.create()
	 * 		.setNetworkParams()
	 * 		.blockReward(Wei.fromEth(5))
	 * 		.targetMiningTime(10000L)
	 * 		.minDifficulty(BigInteger.valueOf(1000))
	 * 		.done()
	 * 		.network(Network.MAINNET)
	 * 		.sender(authorityAddress)
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * }</pre>
	 * 
	 * @return NetworkParamsBuilder for setting network parameters
	 */
	public NetworkParamsBuilder setNetworkParams() {
		return new NetworkParamsBuilder(this);
	}

	/**
	 * Validates required fields before building.
	 * 
	 * @throws CryptoJException if validation fails
	 */
	private void validate() throws CryptoJException {
		if (type == null) {
			throw new CryptoJException("Transaction type is required");
		}
		if (network == null) {
			throw new CryptoJException("Network is required");
		}
		if (sender == null) {
			throw new CryptoJException("Sender address is required");
		}
		if (nonce == null) {
			throw new CryptoJException("Nonce is required");
		}
		if (fee == null) {
			throw new CryptoJException("Fee is required");
		}

		// Type-specific validations
		switch (type) {
			case TRANSFER:
				if (recipient == null) {
					throw new CryptoJException("Recipient is required for TRANSFER transactions");
				}
				break;
			case BIP_CREATE:
				// BIP_CREATE only requires payload (token mint/burn/create/etc are payloads)
				if (payload == null) {
					throw new CryptoJException("Payload is required for " + type + " transactions");
				}
				// BIP_CREATE does NOT require referenceHash
				// BIP_CREATE does NOT support amount at transaction level
				// (TOKEN_MINT/BURN use amount in payload, not in transaction)
				// Amount MUST be strictly null, not zero
				if (amount != null) {
					throw new CryptoJException(
							"BIP_CREATE transactions must have null amount. " +
									"Use payload amount for TOKEN_MINT/BURN operations.");
				}
				break;
			case BIP_VOTE:
				// BIP_VOTE requires payload AND referenceHash (reference to BIP_CREATE
				// proposal)
				if (payload == null) {
					throw new CryptoJException("Payload is required for " + type + " transactions");
				}
				if (referenceHash == null) {
					throw new CryptoJException("Reference hash is required for " + type + " transactions");
				}
				// BIP_VOTE does NOT support amount at transaction level
				// Amount MUST be strictly null, not zero
				if (amount != null) {
					throw new CryptoJException("BIP_VOTE transactions must have null amount.");
				}
				break;
			default:
				// Allow for future transaction types
				break;
		}
	}

	/**
	 * Builds an unsigned transaction.
	 * 
	 * <p>
	 * This creates a transaction without a signature. To create a signed
	 * transaction,
	 * use {@link #sign(PrivateKey)} instead.
	 * 
	 * @return unsigned transaction
	 * @throws CryptoJException if validation fails
	 */
	public Tx buildUnsigned() throws CryptoJException {
		validate();

		return TxImpl.builder()
				.version(version)
				.timestamp(timestamp)
				.type(type)
				.network(network)
				.nonce(nonce)
				.recipient(recipient)
				.amount(amount)
				.tokenAddress(tokenAddress)
				.message(message)
				.fee(fee)
				.payload(payload)
				.referenceHash(referenceHash)
				.signature(null) // Explicitly unsigned
				.build();
	}

	/**
	 * Builds and signs the transaction with the provided private key.
	 * 
	 * <p>
	 * This is the primary method for wallet applications. It validates the
	 * transaction,
	 * builds it, computes the transaction hash, signs it with the private key, and
	 * returns
	 * a complete signed transaction ready for broadcasting.
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>{@code
	 * PrivateKey myKey = PrivateKey.load(mnemonic, password, 0);
	 * 
	 * Tx signedTx = TxBuilder.create()
	 * 		.type(TxType.TRANSFER)
	 * 		.network(Network.MAINNET)
	 * 		.sender(myKey)
	 * 		.recipient(recipientAddress)
	 * 		.amount(Wei.fromEth(1))
	 * 		.fee(Wei.fromEth(0.001))
	 * 		.nonce(1L)
	 * 		.sign(myKey);
	 * 
	 * // Transaction is now ready to broadcast
	 * }</pre>
	 * 
	 * @param privateKey the private key to sign with
	 * @return fully signed transaction
	 * @throws CryptoJException if validation or signing fails
	 */
	public Tx sign(@NonNull PrivateKey privateKey) throws CryptoJException {
		// Build unsigned transaction first
		Tx unsignedTx = buildUnsigned();

		// Verify sender matches private key
		Address derivedAddress = privateKey.getAddress();
		if (!derivedAddress.equals(sender)) {
			throw new CryptoJException(
					String.format("Private key address (%s) does not match sender address (%s)",
							derivedAddress.toChecksumAddress(),
							sender.toChecksumAddress()));
		}

		// Compute transaction hash for signing (excludes signature field)
		Hash txHashForSigning = TxUtil.hashForSigning(unsignedTx);

		// Sign the hash
		Signature signature = privateKey.sign(txHashForSigning);

		// Return a new transaction with the signature included
		return TxImpl.builder()
				.version(version)
				.timestamp(timestamp)
				.type(type)
				.network(network)
				.nonce(nonce)
				.recipient(recipient)
				.amount(amount)
				.tokenAddress(tokenAddress)
				.message(message)
				.fee(fee)
				.payload(payload)
				.referenceHash(referenceHash)
				.signature(signature)
				.build();
	}

	/**
	 * Computes the transaction hash (without signing).
	 * 
	 * <p>
	 * This is useful for previewing what will be signed or for debugging.
	 * 
	 * @return transaction hash that would be signed
	 * @throws CryptoJException if validation fails
	 */
	public Hash computeHash() throws CryptoJException {
		Tx unsignedTx = buildUnsigned();
		return TxUtil.hashForSigning(unsignedTx);
	}

	/**
	 * Estimates the transaction size in bytes.
	 * 
	 * <p>
	 * Note: This creates a temporary unsigned transaction to compute the size.
	 * The actual signed transaction will be slightly larger due to the signature
	 * field.
	 * 
	 * @return estimated size in bytes
	 * @throws CryptoJException if validation fails
	 */
	public int estimateSize() throws CryptoJException {
		Tx unsignedTx = buildUnsigned();
		return TxUtil.size(unsignedTx) + Signature.SIZE; // Add signature size
	}
}
