# Decagon
Crypto Wallet with modern libs, striking the security, and balance of smooth user experiences 
## 🚀 Decagon: Flagship Web3 Mobile Wallet & dApp Browser (2025 Standard)

Decagon is an advanced, non-custodial mobile wallet and dApp browser built to be the comprehensive gateway for the decentralized web. Designed for performance, security, and multi-chain compatibility, it supports the full lifecycle of a Web3 user, from secure key management to complex DeFi interactions and NFT gallery viewing.

---

## 🏗️ Architecture and Technology Stack

Decagon follows a strict **Clean Architecture** (layered architecture) pattern to ensure maximum testability, maintainability, and clear separation of concerns. This design principle isolates business logic from UI and data fetching mechanisms, making the system highly resilient to platform changes.

| Layer | Responsibility | Key Files / Technologies |
| :--- | :--- | :--- |
| **Presentation** | UI, User Interaction, State Management | Jetpack Compose (Material 3), ViewModels, `presentation/screen/*` |
| **Domain** | Core Business Logic, Use Cases, Models | Pure Kotlin, Interfaces, `domain/model/*`, `domain/usecase/*` |
| **Data** | External Interactions (DB, Network, Disk) | Room, Ktor, `data/repository/Impls`, `data/remote/blockchain/*` |
| **Core** | Platform/Cryptographic Utilities | Android Keystore, BIP39/BIP44, Koin, `core/security/*`, `core/crypto/*` |

### Technology Highlights

* **Platform:** Android (Kotlin First)
* **UI Framework:** **Jetpack Compose** (Material 3)
* **Dependency Injection:** **Koin** (chosen for its KMP compatibility and reduced boilerplate over Hilt)
* **Database:** **Room** (Type-safe persistence with Flow support)
* **Networking:** **Ktor Client** (for both traditional APIs and blockchain RPC endpoints)

---

## 🔒 Security and Privacy Infrastructure

Decagon's design prioritizes a **defense-in-depth** strategy, ensuring user keys and funds are protected by multiple, independent security layers.

### Key Management

* **Android Keystore Integration:** Private keys and encrypted wallet seeds are never stored in plaintext. Cryptographic operations leverage the hardware-backed secure enclave via `core/security/KeystoreManager.kt`.
* **Biometric Authentication:** All sensitive operations (e.g., transaction signing) require explicit user authorization via `core/security/BiometricAuthenticator.kt`.
* **Root/Jailbreak Detection:** The application enforces environment integrity checks using `core/security/RootDetector.kt` to prevent execution on compromised devices.
* **Mnemonic Standard:** Uses industry-standard **BIP39/BIP44** for all key generation and derivation paths.

### Transaction Approval Flow (Critical Security Check)

Before any transaction is signed, the user must pass through a strict, multi-step gate:

1.  **DApp Permission Check:** Ensure the connected dApp has the required permissions.
2.  **Transaction Simulation:** The `domain/usecase/transaction/SimulateTransactionUseCase.kt` dry-runs the transaction on a fork RPC to detect failures, scam contracts (e.g., wallet drains), and estimate gas costs *before* signing. This prevents users from wasting gas on doomed transactions.
3.  **Human-Readable Review:** The `TransactionApprovalDialog.kt` displays all monetary effects, network fees, dApp attribution, and simulation results in an easy-to-understand format.
4.  **Biometric Confirmation:** Final authorization via biometric prompt is required before key material is accessed.

---

## 🗺️ Complete Wallet Feature Map

Decagon is built to support the most comprehensive non-custodial feature set available, including advanced Web3 native capabilities and DeFi tooling.

### I. Multi-Chain & Core Management

* **Multi-Chain Support:** Solana, Ethereum, Polygon, Base, Arbitrum, Optimism, and Bitcoin (Ordinals/Runes).
* **HD Wallet:** Supports multiple accounts per chain using BIP44/SLIP-0010 derivation paths.
* **Configuration:** Runtime chain configuration loaded from `assets/chains/*.json` enables easy testing and future custom chain support.
* **Background Sync:** `BalanceSyncWorker.kt` periodically refreshes balances and transaction statuses, ensuring a non-stale UI upon launch.

### II. dApp Browser & Connection

* **Full WebView:** Provides a secure, sandboxed environment for dApps (`presentation/screen/browser/BrowserScreen.kt`).
* **Connection Standards:** Full support for **WalletConnect v2** and **EIP-6963** provider injection.
* **Naming Service:** Native resolution and display for ENS (`.eth`) and Solana Name Service (`.sol`).

### III. DeFi and Advanced Features

| Feature Area | Key Functionality | File Location Example |
| :--- | :--- | :--- |
| **Swapping** | In-app DEX aggregation (Jupiter, 1inch/Li.fi) with limit orders and cross-chain swaps. | `domain/usecase/token/SwapTokensUseCase.kt` |
| **Staking** | Native Solana staking, liquid staking (mSOL, jitoSOL), and Ethereum liquid staking (stETH, rETH). | `domain/model/defi/StakingPosition.kt` |
| **Gas Management** | EIP-1559 priority fee slider (EVM), Solana priority fee/compute unit editor, and future Gas Tank integration. | `data/local/preferences/ChainPreferences.kt` |
| **DeFi Dashboard** | Aggregated total portfolio value, PnL tracking, and position display for yield farming (Orca whirlpools) and debt (Marginfi, Drift). | `presentation/screen/wallet/PositionCard.kt` |

### IV. NFT Handling

* **Unified Gallery:** Displays all SPL/ERC-721/ERC-1155 tokens across all chains.
* **Metadata:** Cached metadata with Rarity score and floor price display.
* **Advanced Ops:** Support for Send, Receive, Burn, Batch Transfer, and Compressed NFTs (cNFTs).

---

## 🧪 Testing and CI/CD Pipeline

The project implements a rigorous testing strategy across all layers to ensure reliability and security.

### Testing Strategy

* **Unit Tests:** JUnit/MockK for isolated testing of Use Cases (`domain/usecase/*`) and Mappers (`data/mapper/*`).
* **Integration Tests:** End-to-end flow verification (e.g., `WalletCreationFlowTest.kt`) ensures that all layers—UI, ViewModel, UseCase, Repository, and Database—work together correctly.
* **Database Tests:** Dedicated tests for DAO implementations ensure data integrity (`database/WalletDaoTest.kt`).

### CI/CD Security Gate

* **Security Scan Workflow:** A dedicated GitHub Actions workflow (`security-scan.yml`) runs on every Pull Request and weekly to automatically check for common crypto misuse errors (e.g., hardcoded private keys, insecure random number generation).
* **ProGuard Rules:** Dedicated `proguard/crypto-rules.pro` files prevent the obfuscation tool from breaking cryptographic and native method functionality.
