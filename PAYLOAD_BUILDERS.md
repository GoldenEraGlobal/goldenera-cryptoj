# TxBuilder - Complete Payload Builders Guide

## 🎯 Overview

TxBuilder provides a complete set of fluent payload builders for all BIP transaction types. Implementation classes (`*Impl`) are hidden - you work only with a clean, user-friendly API.

## 📦 Token Operations

### Token Create
```java
Tx tx = TxBuilder.create()
    .tokenCreate()
        .name("MyToken")
        .symbol("MTK")
        .decimals(18)                // Optional, default: 18
        .website("https://mytoken.io")  // Optional
        .logo("https://mytoken.io/logo.png")  // Optional
        .maxSupply(BigInteger.valueOf(1_000_000_000))  // Optional
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(1L)
    .fee(Amounts.Fees.BIP)
    .sign(myKey);
```

### Token Mint (requires BIP_CREATE approval)
```java
Tx tx = TxBuilder.create()
    .tokenMint()
        .forProposal(bipCreateTxHash)  // ⭐ Reference to approved proposal
        .token(tokenAddress)
        .recipient(recipientAddress)
        .amount(Amounts.tokens(1000))
        .minerFee(Amounts.Fees.STANDARD)
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(2L)
    .fee(Amounts.tokensDecimal("0.005"))
    .sign(myKey);
```

### Token Burn (requires BIP_CREATE approval)
```java
Tx tx = TxBuilder.create()
    .tokenBurn()
        .forProposal(bipCreateTxHash)  // ⭐ Reference to approved proposal
        .token(tokenAddress)
        .from(holderAddress)
        .amount(Amounts.tokens(100))
        .minerFee(Amounts.Fees.STANDARD)
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(3L)
    .fee(Amounts.tokensDecimal("0.005"))
    .sign(myKey);
```

### Token Update
```java
Tx tx = TxBuilder.create()
    .tokenUpdate()
        .token(tokenAddress)
        .name("NewTokenName")  // Optional
        .symbol("NTN")  // Optional
        .website("https://newsite.com")  // Optional
        .logo("https://newsite.com/logo.png")  // Optional
    .done()
    .network(Network.MAINNET)
    .sender(ownerAddress)
    .nonce(4L)
    .sign(myKey);
```

## 🗳️ Governance

### BIP Vote - Approve
```java
Tx tx = TxBuilder.create()
    .vote()
        .approve(bipProposalHash)  // ⭐ Approve proposal
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(1L)
    .sign(myKey);
```

### BIP Vote - Disapprove
```java
Tx tx = TxBuilder.create()
    .vote()
        .disapprove(bipProposalHash)  // ⭐ Reject proposal
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(2L)
    .sign(myKey);
```

## 👤 Address Aliases

### Add Address Alias
```java
Tx tx = TxBuilder.create()
    .addAddressAlias()
        .address(myAddress)
        .alias("myusername")
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(1L)
    .sign(myKey);
```

### Remove Address Alias
```java
Tx tx = TxBuilder.create()
    .removeAddressAlias()
        .alias("oldusername")
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(2L)
    .sign(myKey);
```

## 🔐 Authority Management

### Add Authority
```java
Tx tx = TxBuilder.create()
    .addAuthority()
        .authority(newAuthorityAddress)
    .done()
    .network(Network.MAINNET)
    .sender(currentAuthorityAddress)
    .nonce(1L)
    .sign(myKey);
```

### Remove Authority
```java
Tx tx = TxBuilder.create()
    .removeAuthority()
        .authority(authorityToRemove)
    .done()
    .network(Network.MAINNET)
    .sender(currentAuthorityAddress)
    .nonce(2L)
    .sign(myKey);
```

## ⚙️ Network Parameters

### Set Network Parameters
```java
Tx tx = TxBuilder.create()
    .setNetworkParams()
        .blockReward(Amounts.tokens(5))  // Optional
        .targetMiningTime(10000L)  // Optional - ms between blocks
        .asertHalfLife(144L)  // Optional - difficulty adjustment
        .minDifficulty(BigInteger.valueOf(1000))  // Optional
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(1L)
    .sign(myKey);
```

## ⭐ Reference Hash (forProposal)

Some transactions require `referenceHash` - a reference to an approved BIP_CREATE transaction:

### Transactions requiring proposal:
- ✅ **TOKEN_MINT** - requires `.forProposal()`
- ✅ **TOKEN_BURN** - requires `.forProposal()`
- ✅ **BIP_VOTE** - automatically set by `.approve()` or `.disapprove()`

### Example workflow:
```java
// 1. Create BIP proposal for token mint
Tx proposal = TxBuilder.create()
    .tokenCreate()
        .name("MyToken")
        .symbol("MTK")
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(1L)
    .sign(authorityKey);

Hash proposalHash = TxUtil.hashTx(proposal);

// 2. Authorities vote on the proposal
Tx vote = TxBuilder.create()
    .vote()
        .approve(proposalHash)  // Reference to proposal
    .done()
    .network(Network.MAINNET)
    .sender(authority1Address)
    .nonce(1L)
    .sign(authority1Key);

// 3. Once approved, mint tokens
Tx mint = TxBuilder.create()
    .tokenMint()
        .forProposal(proposalHash)  // Reference to approved proposal
        .token(tokenAddress)
        .recipient(userAddress)
        .amount(Amounts.tokens(1000))
        .minerFee(Amounts.Fees.STANDARD)
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(2L)
    .sign(authorityKey);
```

## 📋 Complete API Reference

| Builder Method | Transaction Type | Requires Proposal? |
|---------------|------------------|-------------------|
| `.tokenCreate()` | BIP_CREATE | ❌ |
| `.tokenMint()` | TOKEN_MINT | ✅ Yes |
| `.tokenBurn()` | TOKEN_BURN | ✅ Yes |
| `.tokenUpdate()` | BIP_CREATE | ❌ |
| `.vote()` | BIP_VOTE | ✅ Implicit |
| `.addAddressAlias()` | BIP_CREATE | ❌ |
| `.removeAddressAlias()` | BIP_CREATE | ❌ |
| `.addAuthority()` | BIP_CREATE | ❌ |
| `.removeAuthority()` | BIP_CREATE | ❌ |
| `.setNetworkParams()` | BIP_CREATE | ❌ |

## ✨ Benefits

✅ **Zero boilerplate** - No `*Impl` classes  
✅ **Governance support** - `.forProposal()` for approval workflow  
✅ **Type-safe** - Compile-time verification  
✅ **User-friendly** - `.approve()` / `.disapprove()` instead of manual setup  
✅ **Complete** - Supports all BIP types  
✅ **Blockchain agnostic** - Uses `Amounts` instead of `Wei.fromEth()`  

## 🚀 Build & Run

```bash
# Compile
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean compile

# Run demo
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn exec:java -Dexec.mainClass="global.goldenera.cryptoj.TxBuilderDemo"
```

---

**Made with ❤️ for GoldenEra Blockchain**
