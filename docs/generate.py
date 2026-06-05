"""
Virtual Wallet -- PDF Documentation Generator
Run: .venv/bin/python generate.py
Output: virtual-wallet-documentation.pdf
"""
from __future__ import annotations

import datetime
from fpdf import FPDF
from fpdf.enums import XPos, YPos

OUTPUT_PATH = "virtual-wallet-documentation.pdf"

# -- Colour palette ----------------------------------------------------------
PRIMARY    = (37,  99,  235)   # blue-600
PRIMARY_LT = (219, 234, 254)   # blue-100
DARK       = (15,  23,  42)    # slate-900
MUTED      = (100, 116, 139)   # slate-500
LIGHT_BG   = (241, 245, 249)   # slate-100
CODE_BG    = (30,  41,  59)    # slate-800
WHITE      = (255, 255, 255)
ROW_ALT    = (248, 250, 252)   # slate-50


# -- Custom FPDF subclass -----------------------------------------------------
class Doc(FPDF):
    current_section: str = ""

    def header(self):
        # Blue top bar
        self.set_fill_color(*PRIMARY)
        self.rect(0, 0, 210, 2, "F")
        # Section label (skip title page)
        if self.page > 2 and self.current_section:
            self.set_y(5)
            self.set_font("Helvetica", "", 8)
            self.set_text_color(*MUTED)
            self.cell(0, 5, self.current_section, align="R", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        self.ln(4)

    def footer(self):
        self.set_y(-12)
        self.set_font("Helvetica", "", 8)
        self.set_text_color(*MUTED)
        self.cell(0, 5, f"Page {self.page}", align="C")


# -- Layout helpers -----------------------------------------------------------

def section_heading(pdf: Doc, text: str, level: int = 1) -> None:
    """H1 (blue, underline bar) or H2 (dark, thin rule)."""
    pdf.ln(4)
    if level == 1:
        pdf.set_font("Helvetica", "B", 18)
        pdf.set_text_color(*PRIMARY)
        pdf.multi_cell(0, 10, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        # Underline bar
        y = pdf.get_y()
        pdf.set_fill_color(*PRIMARY)
        pdf.rect(pdf.l_margin, y, 190, 1, "F")
        pdf.ln(5)
    else:
        pdf.set_font("Helvetica", "B", 13)
        pdf.set_text_color(*DARK)
        pdf.multi_cell(0, 8, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        y = pdf.get_y()
        pdf.set_draw_color(*MUTED)
        pdf.set_line_width(0.3)
        pdf.line(pdf.l_margin, y, pdf.l_margin + 190, y)
        pdf.ln(4)
    pdf.set_text_color(*DARK)


def body_text(pdf: Doc, text: str, indent: int = 0) -> None:
    pdf.set_font("Helvetica", "", 10)
    pdf.set_text_color(*DARK)
    if indent:
        pdf.set_x(pdf.l_margin + indent)
    pdf.multi_cell(190 - indent, 5.5, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.ln(1)


def code_block(pdf: Doc, lines: list[str]) -> None:
    """Dark-background monospaced block."""
    padding = 4
    line_h = 5
    total_h = padding * 2 + line_h * len(lines)
    # Draw background
    x0 = pdf.l_margin
    y0 = pdf.get_y()
    pdf.set_fill_color(*CODE_BG)
    pdf.rect(x0, y0, 190, total_h, "F")
    # Print lines
    pdf.set_font("Courier", "", 8.5)
    pdf.set_text_color(*WHITE)
    pdf.set_y(y0 + padding)
    for line in lines:
        # Truncate very long lines
        if len(line) > 100:
            line = line[:97] + "..."
        pdf.set_x(x0 + padding)
        pdf.cell(190 - padding * 2, line_h, line, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.set_text_color(*DARK)
    pdf.ln(4)


def ascii_diagram(pdf: Doc, lines: list[str]) -> None:
    """Light-background monospaced ASCII art."""
    padding = 5
    line_h = 4.8
    total_h = padding * 2 + line_h * len(lines)
    x0 = pdf.l_margin
    y0 = pdf.get_y()
    pdf.set_fill_color(*LIGHT_BG)
    pdf.set_draw_color(*MUTED)
    pdf.set_line_width(0.3)
    pdf.rect(x0, y0, 190, total_h, "FD")
    pdf.set_font("Courier", "", 8)
    pdf.set_text_color(*DARK)
    pdf.set_y(y0 + padding)
    for line in lines:
        pdf.set_x(x0 + padding)
        pdf.cell(190 - padding * 2, line_h, line, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.set_text_color(*DARK)
    pdf.ln(5)


def two_col_table(
    pdf: Doc,
    headers: list[str],
    rows: list[list[str]],
    col_widths: list[float],
) -> None:
    """Styled table with blue header row and alternating data rows."""
    row_h = 6.5
    # Header
    pdf.set_fill_color(*PRIMARY)
    pdf.set_text_color(*WHITE)
    pdf.set_font("Helvetica", "B", 8.5)
    pdf.set_draw_color(200, 200, 200)
    pdf.set_line_width(0.2)
    for i, h in enumerate(headers):
        pdf.cell(col_widths[i], row_h, f"  {h}", border=1, fill=True,
                 new_x=XPos.RIGHT, new_y=YPos.TOP)
    pdf.ln(row_h)
    # Data rows
    pdf.set_font("Helvetica", "", 8.5)
    pdf.set_text_color(*DARK)
    for r_idx, row in enumerate(rows):
        fill = r_idx % 2 == 1
        pdf.set_fill_color(*ROW_ALT if fill else WHITE)
        for i, cell in enumerate(row):
            txt = str(cell) if cell is not None else ""
            pdf.multi_cell(
                col_widths[i], row_h, f"  {txt}",
                border=1, fill=fill, max_line_height=row_h,
                new_x=XPos.RIGHT, new_y=YPos.TOP,
            )
        pdf.ln(row_h)
    pdf.ln(4)


def bullet_list(pdf: Doc, items: list[str]) -> None:
    pdf.set_font("Helvetica", "", 10)
    pdf.set_text_color(*DARK)
    for item in items:
        x = pdf.get_x()
        pdf.set_x(pdf.l_margin + 4)
        pdf.set_text_color(*PRIMARY)
        pdf.cell(5, 6, "*")
        pdf.set_text_color(*DARK)
        pdf.multi_cell(181, 6, item, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.ln(2)


def info_box(pdf: Doc, title: str, text: str) -> None:
    """Light-bg box with blue left accent."""
    pdf.set_fill_color(*PRIMARY_LT)
    pdf.set_draw_color(*PRIMARY_LT)
    est_lines = max(3, len(text) // 90 + 3)
    h = est_lines * 5.5 + 10
    x0, y0 = pdf.l_margin, pdf.get_y()
    pdf.rect(x0, y0, 190, h, "F")
    # Blue left bar
    pdf.set_fill_color(*PRIMARY)
    pdf.rect(x0, y0, 3, h, "F")
    # Title
    pdf.set_xy(x0 + 7, y0 + 4)
    pdf.set_font("Helvetica", "B", 10)
    pdf.set_text_color(*PRIMARY)
    pdf.cell(0, 5, title, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.set_x(x0 + 7)
    pdf.set_font("Helvetica", "", 9.5)
    pdf.set_text_color(*DARK)
    pdf.multi_cell(183, 5.5, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.ln(5)


# -- Section renderers --------------------------------------------------------

def render_title_page(pdf: Doc) -> None:
    pdf.set_fill_color(*PRIMARY)
    pdf.rect(0, 0, 210, 297, "F")

    # Project name
    pdf.set_y(80)
    pdf.set_font("Helvetica", "B", 48)
    pdf.set_text_color(*WHITE)
    pdf.cell(0, 20, "Virtual Wallet", align="C", new_x=XPos.LMARGIN, new_y=YPos.NEXT)

    # Subtitle
    pdf.set_font("Helvetica", "", 15)
    pdf.set_text_color(188, 212, 254)
    pdf.cell(0, 8, "Full-Stack Financial Application -- Technical Reference",
             align="C", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.ln(8)

    # Horizontal rule
    pdf.set_draw_color(*WHITE)
    pdf.set_line_width(0.5)
    pdf.line(30, pdf.get_y(), 180, pdf.get_y())
    pdf.ln(10)

    # Tech stack badges
    badges = ["Spring Boot 3", "Java 17", "Vue 3", "TypeScript", "MariaDB", "Docker"]
    pdf.set_font("Helvetica", "B", 10)
    total_w = sum(pdf.get_string_width(b) + 16 for b in badges) + (len(badges) - 1) * 4
    x = (210 - total_w) / 2
    y = pdf.get_y()
    for badge in badges:
        bw = pdf.get_string_width(badge) + 16
        pdf.set_fill_color(255, 255, 255, )
        pdf.set_fill_color(59, 130, 246)
        pdf.rect(x, y, bw, 8, "F")
        pdf.set_text_color(*WHITE)
        pdf.set_xy(x, y + 1)
        pdf.cell(bw, 6, badge, align="C")
        x += bw + 4

    # Date bottom-right
    pdf.set_xy(0, 265)
    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(188, 212, 254)
    pdf.cell(0, 6, f"Generated {datetime.date.today().strftime('%B %d, %Y')}",
             align="C")


def render_overview(pdf: Doc) -> None:
    pdf.current_section = "1. Project Overview"
    pdf.add_page()
    pdf.start_section("1. Project Overview")
    section_heading(pdf, "1. Project Overview")

    body_text(pdf,
        "Virtual Wallet is a full-stack digital banking application that lets users "
        "register, verify their identity via email, manage multi-currency wallets (EUR, USD, GBP), "
        "link debit/credit cards, top up their wallets, and transfer funds between users with "
        "automatic real-time currency conversion. An admin panel allows staff to monitor all "
        "users and transactions across the system.")

    body_text(pdf,
        "The backend is a Spring Boot 3 REST API (Java 17) backed by MariaDB, secured with "
        "stateless JWT authentication. The frontend is a Vue 3 + TypeScript SPA using Pinia "
        "for state management, Axios for HTTP, and Vue Router 4 for navigation. External currency "
        "conversion rates are fetched from ExchangeRate-API v6 in real time on each cross-currency transfer.")

    section_heading(pdf, "System Architecture", level=2)
    ascii_diagram(pdf, [
        "  +-------------------------------------------------------+",
        "  |              BROWSER  (Vue 3 + TypeScript)            |",
        "  |  LoginPage  RegisterPage  DashboardPage  WalletsPage  |",
        "  |  CardsPage  TransactionsPage  TransferPage  Profile   |",
        "  |  Admin: UsersPage  Admin: TransactionsPage            |",
        "  +------------------------+------------------------------+",
        "                           | HTTPS  /api/v0/*",
        "  +------------------------v------------------------------+",
        "  |           SPRING BOOT BACKEND  :8080                 |",
        "  |  AuthController  WalletController  CardController    |",
        "  |  TransactionController  TransferController           |",
        "  |  UserController                                      |",
        "  |  JWT Filter -> SecurityFilterChain -> Service Layer  |",
        "  +------------+-----------------------------+-----------+",
        "               | JPA / Hibernate             | RestClient",
        "  +------------v-----------+    +------------v----------+",
        "  |  MariaDB 11  :3306     |    |  ExchangeRate-API v6  |",
        "  |  virtual_wallet DB     |    |  Real-time FX rates   |",
        "  +------------------------+    +-----------------------+",
    ])

    section_heading(pdf, "Technology Stack", level=2)
    two_col_table(pdf,
        ["Layer", "Technology"],
        [
            ["Backend framework", "Spring Boot 3.x (Java 17)"],
            ["Database", "MariaDB 11 (via Docker)"],
            ["ORM", "Spring Data JPA / Hibernate"],
            ["Security", "Spring Security + JWT (HS256, 24 h expiry)"],
            ["Email", "Spring Mail (Gmail SMTP)"],
            ["Currency conversion", "ExchangeRate-API v6"],
            ["Frontend framework", "Vue 3 (Composition API) + Vite"],
            ["Language", "TypeScript"],
            ["State management", "Pinia with persisted state plugin"],
            ["HTTP client", "Axios with request/response interceptors"],
            ["Routing", "Vue Router 4"],
            ["UI icons", "Lucide-vue-next"],
            ["Dialogs", "SweetAlert2 (custom themed)"],
            ["Containerisation", "Docker + Docker Compose"],
        ],
        [70, 120],
    )


def render_getting_started(pdf: Doc) -> None:
    pdf.current_section = "2. Getting Started"
    pdf.add_page()
    pdf.start_section("2. Getting Started")
    section_heading(pdf, "2. Getting Started")

    section_heading(pdf, "Prerequisites", level=2)
    bullet_list(pdf, [
        "Java 17 JDK",
        "Docker Desktop (for MariaDB)",
        "Node.js 20+ and npm",
        "Git",
        "An ExchangeRate-API key (free tier at exchangerate-api.com)",
        "A Gmail account for SMTP (or any SMTP provider)",
    ])

    section_heading(pdf, "Backend Setup", level=2)
    body_text(pdf, "1. Clone the repository and navigate to the backend directory:")
    code_block(pdf, [
        "git clone <repo-url>",
        "cd virtual-wallet/backend",
    ])

    body_text(pdf, "2. Create the secrets file (not committed to source control):")
    code_block(pdf, [
        "# src/main/resources/application-secrets.properties",
        "exchange.rate.api.key=YOUR_EXCHANGERATE_API_KEY",
        "spring.mail.username=your@gmail.com",
        "spring.mail.password=your_app_password",
    ])

    body_text(pdf, "3. Start the MariaDB database container:")
    code_block(pdf, ["docker-compose up -d"])

    body_text(pdf, "4. Build and run the backend:")
    code_block(pdf, [
        "./gradlew build",
        "./gradlew bootRun",
        "# API available at http://localhost:8080/api/v0",
    ])

    section_heading(pdf, "Frontend Setup", level=2)
    code_block(pdf, [
        "cd ../frontend",
        "npm install",
        "npm run dev",
        "# Dev server at http://localhost:5173",
    ])

    section_heading(pdf, "Seeded Users (Development)", level=2)
    body_text(pdf, "The DataSeeder configuration creates the following accounts on first startup:")
    two_col_table(pdf,
        ["Role", "Username", "Email", "Password", "Status"],
        [
            ["ADMIN", "admin", "admin@example.com", "Admin1234!", "Verified"],
            ["USER", "johndoe", "john@example.com", "John1234!", "Verified"],
            ["USER", "janedoe", "jane@example.com", "Jane1234!", "Verified"],
        ],
        [22, 28, 52, 36, 24],
    )

    section_heading(pdf, "Running Tests", level=2)
    code_block(pdf, [
        "# All tests",
        "./gradlew test",
        "",
        "# Single test class",
        "./gradlew test --tests example.backend.services.AuthServiceTests",
        "",
        "# Verbose output",
        "./gradlew test --info",
    ])
    info_box(pdf, "Test Database",
        "All tests use an H2 in-memory database configured via @ExtendWith(MockitoExtension.class). "
        "Service tests inject mocked repositories so no running MariaDB instance is required.")


def render_auth(pdf: Doc) -> None:
    pdf.current_section = "3. Authentication & Security"
    pdf.add_page()
    pdf.start_section("3. Authentication & Security")
    section_heading(pdf, "3. Authentication & Security")

    section_heading(pdf, "JWT Token Flow", level=2)
    body_text(pdf,
        "Authentication is stateless. The backend issues a signed JWT on login; "
        "every subsequent request must include it in the Authorization header. "
        "The server never stores session state.")

    ascii_diagram(pdf, [
        "  Client                  Backend                  DB",
        "    |                        |                      |",
        "    |-- POST /auth/login ---->|                      |",
        "    |   {email, password}    |-- findByEmail() ----->|",
        "    |                        |<--- User -------------|",
        "    |                        | BCrypt.matches()      |",
        "    |                        | JWT.generate()        |",
        "    |<-- {token, user} -------|                      |",
        "    |                        |                      |",
        "    |-- GET /api/v0/* ------->|                      |",
        "    |  Authorization:        | JwtAuthFilter         |",
        "    |  Bearer <token>        | validates HS256       |",
        "    |                        | sets SecurityContext  |",
        "    |<-- 200 OK / 403 --------|                      |",
    ])

    info_box(pdf, "JWT Token Details",
        "Algorithm: HMAC-SHA256 (HS256)  |  Expiry: 24 hours  |  "
        "Claims: sub (username), roles (array)  |  "
        "Token is also accepted from an HTTP cookie named 'jwt' as a fallback. "
        "The JwtAuthenticationFilter extends OncePerRequestFilter and runs on every request.")

    section_heading(pdf, "Email Verification (OTP)", level=2)
    body_text(pdf,
        "New users must verify their email address before accessing protected features. "
        "On registration the backend generates a 6-digit code (ThreadLocalRandom), stores it "
        "with a 15-minute expiry, and sends it via Gmail SMTP. The user submits the code to "
        "POST /api/v0/auth/verify. On success the account is marked verified and a base EUR "
        "wallet is automatically created.")

    ascii_diagram(pdf, [
        "  Register --> sendVerificationEmail() --> User submits code",
        "            (6-digit OTP, 15 min expiry)    (POST /auth/verify)",
        "                                              |",
        "                                     isVerified = true",
        "                                     createBaseEURWallet()",
    ])

    section_heading(pdf, "@RequiresVerifiedAccount Annotation", level=2)
    body_text(pdf,
        "Sensitive operations (wallet creation, top-up, card management, transfers) are "
        "protected by the custom @RequiresVerifiedAccount annotation backed by a Spring AOP "
        "aspect. Before any annotated service method executes, the aspect retrieves the "
        "authenticated user, checks isVerified and isBlocked, and throws "
        "AccountNotVerifiedException (HTTP 403) or UserBlockedException (HTTP 403) accordingly.")

    code_block(pdf, [
        "@RequiresVerifiedAccount  // triggers AOP check before execution",
        "public Wallet createWallet(Wallet wallet) { ... }",
        "",
        "// Aspect logic (RequiresVerifiedAccountAspect.java):",
        "@Before(\"@annotation(RequiresVerifiedAccount)\")",
        "public void check() {",
        "    User user = authUtils.getAuthenticatedUser();",
        "    if (!user.isVerified())  throw new AccountNotVerifiedException();",
        "    if (user.isBlocked())    throw new UserBlockedException();",
        "}",
    ])

    section_heading(pdf, "Roles & Permissions", level=2)
    two_col_table(pdf,
        ["Role", "Capabilities"],
        [
            ["ROLE_USER",
             "Login, register, verify, view own profile, manage own wallets/cards, "
             "make transfers, view own transactions, search users (public info only)"],
            ["ROLE_ADMIN",
             "All USER capabilities plus: list all users, get any user by ID, "
             "create users, block/unblock users, view all transactions, "
             "get transactions by any user, view any card by ID"],
        ],
        [28, 162],
    )

    section_heading(pdf, "Password Encoding", level=2)
    body_text(pdf,
        "All passwords are encoded with BCrypt before storage (BCryptPasswordEncoder bean). "
        "Plain-text passwords are never persisted. Login validation uses BCrypt.matches(raw, encoded). "
        "Admin-created users use a pre-set BCrypt-encoded password.")


def render_features(pdf: Doc) -> None:
    pdf.current_section = "4. Features"
    pdf.add_page()
    pdf.start_section("4. Features")
    section_heading(pdf, "4. Core Features")

    # --- Wallets ---
    section_heading(pdf, "Multi-Currency Wallets", level=2)
    body_text(pdf,
        "Each verified user can hold up to three wallets -- one per supported currency "
        "(EUR, USD, GBP). A base EUR wallet is created automatically on first verification. "
        "Additional wallets can be named (max 20 characters) and are created immediately. "
        "Deletion requires a zero balance and at least one remaining wallet.")
    two_col_table(pdf,
        ["Rule", "Detail"],
        [
            ["Currencies supported", "EUR (EUR), USD ($), GBP (GBP)"],
            ["Unique constraint", "One wallet per user per currency (DB-enforced)"],
            ["Balance type", "BigDecimal with precision 19, scale 2"],
            ["Currency immutable", "Cannot change currency after wallet creation"],
            ["Deletion guard", "Balance must be 0.00; user must retain >=1 wallet"],
            ["Auto-wallet", "EUR wallet created automatically after email verification"],
        ],
        [60, 130],
    )

    # --- Cards ---
    section_heading(pdf, "Card Tokenisation", level=2)
    body_text(pdf,
        "Users link debit/credit cards to their account. The full card number is never "
        "stored. The PaymentService tokenises each card on addition: it generates a "
        "unique tok_<UUID> token and a SHA-256 fingerprint of (cardNumber | brand) for "
        "deduplication. Only the token, fingerprint, last 4 digits, brand, and expiry are persisted.")

    two_col_table(pdf,
        ["Brand", "Number Prefix", "Length"],
        [
            ["VISA", "Starts with 4", "13 or 16 digits"],
            ["MASTERCARD", "51-55 or 22-27", "16 digits"],
            ["AMEX", "34 or 37", "15 digits"],
            ["DISCOVER", "6011 or 65", "16 digits"],
        ],
        [40, 60, 90],
    )

    body_text(pdf,
        "Duplicate prevention: if a user tries to add the same physical card twice "
        "(same fingerprint), a DuplicateException (HTTP 409) is thrown. Expired cards are "
        "rejected at creation time.")

    # --- Top-Up ---
    section_heading(pdf, "Wallet Top-Up", level=2)
    body_text(pdf,
        "A user may top up any of their own wallets using a linked card. The endpoint "
        "POST /api/v0/wallets/top-up receives the wallet ID, card ID, and amount. "
        "The PaymentService.charge() simulates a card charge (no live gateway), the wallet "
        "balance is incremented, and a TOP_UP transaction record is persisted with status SUCCESSFUL.")

    code_block(pdf, [
        "// TopUpRequest DTO",
        "{",
        "  \"wallet_id\": 1,",
        "  \"card_id\":   2,",
        "  \"amount\":    100.00",
        "}",
    ])

    # --- Transfers ---
    section_heading(pdf, "User-to-User Transfers", level=2)
    body_text(pdf,
        "Transfers move funds between two verified users. The sender specifies their "
        "source wallet, the recipient's username, and the amount. If the currencies differ, "
        "the ConversionService fetches a live rate from ExchangeRate-API v6 and applies it "
        "automatically. The recipient receives funds in their matching-currency wallet, or "
        "their first available wallet if no match exists.")

    ascii_diagram(pdf, [
        "  TransferReq {fromWalletId, toUsername, amount}",
        "       |",
        "       v",
        "  1. Load source wallet (pessimistic lock)",
        "  2. Verify authenticated user owns source wallet",
        "  3. Load recipient by username -- must be verified",
        "  4. Find recipient wallet (prefer same currency -> fallback first)",
        "  5. Validate: different wallets, positive amount, sufficient balance",
        "  6. Convert amount if currencies differ (ExchangeRate-API)",
        "  7. Debit sender:   fromWallet.balance -= amount",
        "  8. Credit receiver: toWallet.balance   += convertedAmount",
        "  9. Persist TRANSFER_OUT tx on source wallet",
        " 10. Persist TRANSFER_IN  tx on dest wallet",
    ])

    info_box(pdf, "Concurrency Safety",
        "Wallets involved in a transfer are loaded with a pessimistic write lock "
        "(SELECT ... FOR UPDATE via WalletRepository.findByIdForUpdate). This prevents "
        "race conditions when two concurrent transfers attempt to debit the same wallet simultaneously.")

    # --- Transactions ---
    section_heading(pdf, "Transaction History & Filtering", level=2)
    two_col_table(pdf,
        ["Transaction Type", "Description"],
        [
            ["TOP_UP", "Funds loaded from a linked card"],
            ["WITHDRAWAL", "Funds withdrawn (reserved for future use)"],
            ["TRANSFER_OUT", "Funds sent to another user (negative amount)"],
            ["TRANSFER_IN", "Funds received from another user (positive amount)"],
            ["FEE", "Service fee (reserved for future use)"],
            ["REFUND", "Refund credit (reserved for future use)"],
        ],
        [38, 152],
    )

    two_col_table(pdf,
        ["Status", "Meaning"],
        [
            ["PENDING", "Created but not yet settled (default on creation)"],
            ["SUCCESSFUL", "Operation completed successfully"],
            ["FAILED", "Operation failed (e.g., insufficient funds at gateway level)"],
        ],
        [30, 160],
    )

    body_text(pdf, "Available filter parameters on GET /api/v0/transactions:")
    two_col_table(pdf,
        ["Parameter", "Type", "Description"],
        [
            ["type", "Enum", "Filter by transaction type (TOP_UP, TRANSFER_IN, ...)"],
            ["status", "Enum", "Filter by status (PENDING, SUCCESSFUL, FAILED)"],
            ["from", "ISO DateTime", "Include only transactions after this timestamp"],
            ["to", "ISO DateTime", "Include only transactions before this timestamp"],
            ["transactionId", "Long", "Exact transaction ID lookup"],
            ["targetUserId", "Long", "Admin: filter by wallet owner's user ID"],
            ["page", "Integer", "Zero-based page number (Spring Pageable)"],
            ["size", "Integer", "Page size (default 20)"],
        ],
        [30, 28, 132],
    )


def render_api(pdf: Doc) -> None:
    pdf.current_section = "5. API Reference"
    pdf.add_page()
    pdf.start_section("5. Backend API Reference")
    section_heading(pdf, "5. Backend API Reference")
    body_text(pdf, "Base URL: http://localhost:8080/api/v0  |  All requests/responses use JSON (snake_case field names).")

    # Auth
    section_heading(pdf, "Auth Endpoints  (/api/v0/auth)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Description"],
        [
            ["POST", "/auth/login",    "None", "Login with email + password. Returns JWT token and user profile."],
            ["POST", "/auth/register", "None", "Register a new account. Sends a 6-digit OTP to the given email."],
            ["POST", "/auth/verify",   "None", "Submit the OTP code to verify the account and unlock features."],
            ["GET",  "/auth/me",       "JWT",  "Return the authenticated user's own profile (PrivateUserDto)."],
        ],
        [16, 32, 14, 128],
    )

    # Wallets
    section_heading(pdf, "Wallet Endpoints  (/api/v0/wallets)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Description"],
        [
            ["GET",    "/wallets",       "JWT", "List all wallets belonging to the authenticated user."],
            ["GET",    "/wallets/{id}",  "JWT", "Get a specific wallet by ID (must be owner)."],
            ["POST",   "/wallets",       "JWT+Verified", "Create a new wallet. Body: {name?, currency?}. EUR default."],
            ["DELETE", "/wallets/{id}",  "JWT+Verified", "Delete wallet. Fails if balance > 0 or it is the last wallet."],
            ["POST",   "/wallets/top-up","JWT+Verified", "Top up wallet from a card. Body: {wallet_id, card_id, amount}."],
        ],
        [16, 38, 36, 100],
    )

    # Cards
    section_heading(pdf, "Card Endpoints  (/api/v0/cards)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Description"],
        [
            ["GET",    "/cards",              "JWT",       "List all cards for the authenticated user."],
            ["GET",    "/cards/{cardId}",     "JWT+Admin", "Get any card by ID (admin only)."],
            ["GET",    "/cards/users/{userId}","JWT+Admin","Get all cards belonging to a specific user (admin)."],
            ["POST",   "/cards",              "JWT+Verified","Add a new card. Tokenised; full number never stored."],
            ["DELETE", "/cards/{cardId}",     "JWT",       "Remove a card (card owner only)."],
        ],
        [16, 44, 36, 94],
    )

    # Transactions
    section_heading(pdf, "Transaction Endpoints  (/api/v0/transactions)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Description"],
        [
            ["GET", "/transactions",           "JWT",       "Paginated, filterable list of the current user's transactions."],
            ["GET", "/transactions/{id}",      "JWT",       "Get one of the current user's transactions by ID."],
            ["GET", "/transactions/all",       "JWT+Admin", "Paginated, filterable list of ALL transactions (admin)."],
            ["GET", "/transactions/admin/{id}","JWT+Admin", "Get any transaction by ID regardless of owner (admin)."],
        ],
        [16, 58, 36, 80],
    )

    # Transfers
    section_heading(pdf, "Transfer Endpoint  (/api/v0/transfers)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Request Body"],
        [
            ["POST", "/transfers", "JWT+Verified",
             "{from_wallet_id: Long, to_username: String, amount: Decimal}"],
        ],
        [16, 28, 36, 110],
    )
    body_text(pdf,
        "Both sender and recipient must be verified. Insufficient funds throw "
        "ImpossibleOperationException (HTTP 400). Currency conversion is applied automatically.")

    # Users
    section_heading(pdf, "User Endpoints  (/api/v0/users)", level=2)
    two_col_table(pdf,
        ["Method", "Path", "Auth", "Description"],
        [
            ["GET",    "/users",          "JWT+Admin", "Paginated list of all users."],
            ["GET",    "/users/{userId}", "JWT+Admin", "Get any user's full profile by ID."],
            ["GET",    "/users/search",   "JWT",       "Search users by username/name. Query param: q=<string>."],
            ["POST",   "/users",          "JWT+Admin", "Admin creates a new pre-verified user."],
            ["PUT",    "/users",          "JWT",       "Update own profile. Fields: email, phone_number, photo_url, password."],
            ["DELETE", "/users",          "JWT",       "Delete own account (cascades to wallets and cards)."],
        ],
        [16, 40, 36, 98],
    )

    section_heading(pdf, "Error Response Format", level=2)
    body_text(pdf, "All error responses follow a consistent structure:")
    code_block(pdf, [
        "{",
        '  "status":  403,',
        '  "message": "Account is not verified",',
        '  "uri":     "/api/v0/wallets",',
        '  "errors":  { "field": "validation message" }',
        "}",
    ])
    two_col_table(pdf,
        ["Exception", "HTTP Status", "Scenario"],
        [
            ["AuthorizationException",       "403", "User does not own the requested resource"],
            ["AccountNotVerifiedException",  "403", "Account requires email verification"],
            ["UserBlockedException",         "403", "User account has been blocked by admin"],
            ["DuplicateException",           "409", "Unique constraint violated (e.g. same card twice)"],
            ["EntityNotFoundException",      "404", "Requested resource does not exist"],
            ["ImpossibleOperationException", "400", "Invalid operation (negative balance, same-wallet transfer)"],
            ["InvalidCardException",         "400", "Card number fails brand validation"],
            ["CardExpiredException",         "400", "Card expiry date is in the past"],
            ["ExchangeRateApiException",     "503", "ExchangeRate-API is unreachable"],
        ],
        [62, 28, 100],
    )


def render_data_models(pdf: Doc) -> None:
    pdf.current_section = "6. Data Models"
    pdf.add_page()
    pdf.start_section("6. Data Models")
    section_heading(pdf, "6. Data Models")

    body_text(pdf,
        "All entities are JPA-mapped to a MariaDB 11 schema. Spring Data JPA handles DDL "
        "via spring.jpa.hibernate.ddl-auto=update. The naming strategy converts camelCase "
        "Java fields to snake_case column names automatically.")

    # User
    section_heading(pdf, "User", level=2)
    two_col_table(pdf,
        ["Field", "Type", "Constraints", "Notes"],
        [
            ["id",                      "Long",          "PK, auto-increment",            ""],
            ["firstName",               "String",        "max 50",                         ""],
            ["lastName",                "String",        "max 50",                         ""],
            ["username",                "String",        "unique, not null",               "Used for login and search"],
            ["password",                "String",        "not null",                       "BCrypt-hashed; never plain-text"],
            ["email",                   "String",        "unique, not null, @Email",       ""],
            ["phoneNumber",             "String",        "unique, E.164 format",           "+[1-9]\\d{7,14}"],
            ["photoUrl",                "String",        "optional, must start https://",  ""],
            ["verificationCode",        "String",        "nullable",                       "6-digit OTP"],
            ["verificationCodeExpiresAt","LocalDateTime","nullable",                       "15 min initial, 5 min resend"],
            ["createdAt",               "LocalDateTime", "@CreationTimestamp",             "Auto-set on insert"],
            ["isBlocked",               "boolean",       "default false",                  "Admin-controlled"],
            ["isVerified",              "boolean",       "default false",                  "Set to true after OTP verify"],
            ["cards",                   "Set<Card>",     "OneToMany, cascade ALL",         "Deleted with user"],
            ["wallets",                 "Set<Wallet>",   "OneToMany, cascade ALL",         "Deleted with user"],
            ["roles",                   "Set<Role>",     "ManyToMany",                     "ROLE_USER | ROLE_ADMIN"],
        ],
        [42, 32, 56, 60],
    )

    # Wallet
    section_heading(pdf, "Wallet", level=2)
    two_col_table(pdf,
        ["Field", "Type", "Constraints", "Notes"],
        [
            ["id",       "Long",       "PK, auto-increment",              ""],
            ["owner",    "User",       "ManyToOne, not null",             "FK: user.id"],
            ["name",     "String",     "max 20, nullable",                "Optional user-given label"],
            ["balance",  "BigDecimal", "precision 19, scale 2, default 0","Never negative"],
            ["currency", "Currency",   "Enum: EUR / USD / GBP, not null", "Immutable after creation"],
            ["transactions","Set<Transaction>","OneToMany",               "All transactions on this wallet"],
        ],
        [28, 32, 50, 80],
    )
    info_box(pdf, "Unique Constraint",
        "The combination of (owner, currency) has a database-level unique constraint. "
        "Attempting to create a second EUR wallet for the same user throws a DuplicateException (HTTP 409).")

    # Card
    section_heading(pdf, "Card", level=2)
    two_col_table(pdf,
        ["Field", "Type", "Constraints", "Notes"],
        [
            ["id",              "Long",      "PK, auto-increment",     ""],
            ["token",           "String",    "unique, not null",       "tok_<UUID> -- payment gateway reference"],
            ["fingerprint",     "String",    "unique per user",        "SHA-256(cardNumber | brand)"],
            ["cardBrand",       "CardBrand", "Enum, not null",         "VISA / MASTERCARD / AMEX / DISCOVER"],
            ["last4",           "String",    "not null",               "Last 4 digits for display"],
            ["expirationMonth", "int",       "not null",               ""],
            ["expirationYear",  "int",       "not null",               ""],
            ["cardHolder",      "User",      "ManyToOne, not null",    "FK: user.id"],
        ],
        [36, 28, 40, 86],
    )

    # Transaction
    section_heading(pdf, "Transaction", level=2)
    two_col_table(pdf,
        ["Field", "Type", "Constraints", "Notes"],
        [
            ["id",               "Long",            "PK, auto-increment",              ""],
            ["amount",           "BigDecimal",       "precision 19, scale 2",           "Negative for debits"],
            ["currency",         "Currency",         "Enum: EUR / USD / GBP",           "Currency of the amount"],
            ["type",             "TransactionType",  "Enum, not null",                  "See type table above"],
            ["status",           "TransactionStatus","Enum, default PENDING",           "PENDING / SUCCESSFUL / FAILED"],
            ["timestamp",        "LocalDateTime",    "@CreationTimestamp",              "Auto-set on insert"],
            ["wallet",           "Wallet",           "ManyToOne, not null",             "Primary wallet involved"],
            ["counterpartyWallet","Wallet",          "ManyToOne, nullable",             "Other wallet (transfers only)"],
        ],
        [38, 32, 44, 76],
    )

    # Role
    section_heading(pdf, "Role", level=2)
    two_col_table(pdf,
        ["Field", "Type", "Notes"],
        [
            ["id",   "Long",  "PK, auto-increment"],
            ["name", "ERole", "ROLE_USER or ROLE_ADMIN (Enum)"],
        ],
        [30, 30, 130],
    )

    # ER Diagram
    section_heading(pdf, "Entity Relationship Summary", level=2)
    ascii_diagram(pdf, [
        "  User  1 ---< *  Wallet       (user owns wallets)",
        "  User  1 ---< *  Card         (user owns cards)",
        "  User  * >---< *  Role        (user has roles, ManyToMany)",
        "  Wallet 1 ---< *  Transaction (wallet has transactions)",
        "  Transaction >--- Wallet      (counterpartyWallet, nullable)",
    ])


def render_frontend(pdf: Doc) -> None:
    pdf.current_section = "7. Frontend Architecture"
    pdf.add_page()
    pdf.start_section("7. Frontend Architecture")
    section_heading(pdf, "7. Frontend Architecture")

    section_heading(pdf, "Technology Stack", level=2)
    two_col_table(pdf,
        ["Package", "Purpose"],
        [
            ["vue@3",                   "Composition API, reactivity, template compiler"],
            ["vite",                    "Build tool and dev server (HMR)"],
            ["typescript",              "Static typing throughout"],
            ["pinia",                   "Centralised state management"],
            ["pinia-plugin-persistedstate", "Persists auth store to localStorage"],
            ["vue-router@4",            "Client-side routing with navigation guards"],
            ["axios",                   "HTTP client with interceptors"],
            ["lucide-vue-next",         "Icon set (consistent throughout UI)"],
            ["sweetalert2",             "Confirmation and notification dialogs (custom-themed)"],
        ],
        [62, 128],
    )

    section_heading(pdf, "Directory Structure", level=2)
    code_block(pdf, [
        "frontend/src/",
        "  api/          # Axios modules per domain (auth, wallets, cards, ...)",
        "  assets/       # base.css (design tokens) + main.css (global components)",
        "  components/   # AppPagination.vue",
        "  composables/  # Business logic hooks (useWallets, useCards, useTransfer, ...)",
        "  layouts/      # DashboardLayout.vue (sidebar + main area)",
        "  pages/        # Full-page route components",
        "  router/       # index.ts (routes + beforeEach guard)",
        "  stores/       # auth.ts + wallet.ts (Pinia)",
        "  types/        # index.ts (all TypeScript interfaces / DTOs)",
    ])

    section_heading(pdf, "Pages & Routes", level=2)
    two_col_table(pdf,
        ["Route", "Component", "Guard", "Description"],
        [
            ["/login",              "LoginPage",             "Guest only",    "Email + password login"],
            ["/register",           "RegisterPage",          "Guest only",    "Registration + password strength check"],
            ["/verify",             "VerifyPage",            "Guest only",    "OTP submission + auto-redirect"],
            ["/",                   "DashboardPage",         "Auth",          "Balance overview, recent transactions"],
            ["/wallets",            "WalletsPage",           "Auth",          "Create, top-up, delete wallets"],
            ["/cards",              "CardsPage",             "Auth",          "Add / remove cards with live preview"],
            ["/transactions",       "TransactionsPage",      "Auth",          "Filtered, paginated transaction history"],
            ["/transfer",           "TransferPage",          "Auth",          "User search + multi-step transfer form"],
            ["/profile",            "ProfilePage",           "Auth",          "View own profile and verification status"],
            ["/admin/users",        "Admin/UsersPage",       "Auth + Admin",  "Search and list all users"],
            ["/admin/transactions", "Admin/TransactionsPage","Auth + Admin",  "Full transaction ledger"],
        ],
        [36, 42, 22, 90],
    )

    section_heading(pdf, "Pinia Stores", level=2)
    body_text(pdf, "auth store  (stores/auth.ts)  --  persisted to localStorage")
    two_col_table(pdf,
        ["State / Getter / Action", "Type", "Description"],
        [
            ["token",            "string | null", "JWT from login response"],
            ["user",             "PrivateUserDto | null", "Current user's profile"],
            ["emailForVerification", "string | null", "Email used during verify step"],
            ["isAuthenticated",  "computed bool", "True when token is non-null"],
            ["isAdmin",          "computed bool", "True when user has ROLE_ADMIN"],
            ["isVerified",       "computed bool", "True when user.is_verified === true"],
            ["login(creds)",     "action", "Call authApi.login, store token + user"],
            ["logout()",         "action", "Clear token and user from state + localStorage"],
            ["fetchMe()",        "action", "Refresh user profile from GET /auth/me"],
        ],
        [54, 42, 94],
    )

    body_text(pdf, "wallet store  (stores/wallet.ts)  --  30-second client-side cache")
    two_col_table(pdf,
        ["State / Action", "Description"],
        [
            ["wallets: PrivateWalletDto[]", "Array of the authenticated user's wallets"],
            ["fetchWallets({ force })",     "Fetch wallets; skip if cache < 30 s old unless force=true"],
            ["createWallet(payload)",       "POST /wallets, append result to state"],
            ["deleteWallet(id)",            "DELETE /wallets/:id, remove from state"],
            ["topUp(payload)",              "POST /wallets/top-up, then force-refetch wallets"],
        ],
        [70, 120],
    )

    section_heading(pdf, "API Client (api/client.ts)", level=2)
    body_text(pdf,
        "A configured Axios instance is shared across all API modules. "
        "A request interceptor automatically injects Authorization: Bearer <token> from the auth store. "
        "A response interceptor catches HTTP 401 and calls authStore.logout() + router.push('/login') "
        "to handle token expiry transparently.")

    code_block(pdf, [
        "// Request interceptor",
        "client.interceptors.request.use(config => {",
        "  const token = authStore.token;",
        "  if (token) config.headers.Authorization = `Bearer ${token}`;",
        "  return config;",
        "});",
        "",
        "// Response interceptor",
        "client.interceptors.response.use(null, error => {",
        "  if (error.response?.status === 401) {",
        "    authStore.logout();",
        "    router.push('/login');",
        "  }",
        "  return Promise.reject(error);",
        "});",
    ])

    section_heading(pdf, "Key Composables", level=2)
    two_col_table(pdf,
        ["Composable", "Key Responsibilities"],
        [
            ["useWallets",          "Wallet CRUD modals, top-up modal, currency symbols, quick-add amounts"],
            ["useCards",            "Card CRUD, brand detection (VISA/MC/AMEX), live card preview, expiry validation"],
            ["useTransfer",         "Recipient search (300 ms debounce), wallet selection, transfer submission"],
            ["useTransactions",     "Filter state (type/status/dates), 30-second page cache, smart pagination"],
            ["useTransactionDetail","Lazy-loads full transaction on row expand; caches per ID"],
            ["useDashboard",        "Parallel load: wallets + recent 5 transactions + card count"],
            ["useFormatters",       "formatCurrency (Intl), formatDate, txLabel, statusLabel, amountClass"],
            ["useDialog",           "SweetAlert2 wrapper: confirm(), error(), success() with custom theme"],
        ],
        [40, 150],
    )

    section_heading(pdf, "Design System", level=2)
    body_text(pdf,
        "All CSS custom properties are defined in assets/base.css. "
        "Global component classes (buttons, inputs, modals, spinners) are in assets/main.css. "
        "SweetAlert2 is themed with custom CSS class overrides (.vw-popup, .vw-btn-confirm, etc.).")

    two_col_table(pdf,
        ["Token", "Value", "Usage"],
        [
            ["--color-primary", "#6366f1 (indigo-500)", "Buttons, links, active states"],
            ["--color-success", "#16a34a (green-600)",  "Positive amounts, confirmed status"],
            ["--color-danger",  "#dc2626 (red-600)",    "Debit amounts, errors, delete actions"],
            ["--color-warning", "#d97706 (amber-600)",  "Pending status, unverified badge"],
            ["--surface-main",  "#ffffff",              "Card / modal backgrounds"],
            ["--surface-soft",  "#f8fafc",              "Page background"],
            ["--radius-md",     "10px",                 "Cards, modals"],
            ["--radius-xl",     "20px",                 "Hero cards, large panels"],
        ],
        [38, 44, 108],
    )


def render_deployment(pdf: Doc) -> None:
    pdf.current_section = "8. Deployment"
    pdf.add_page()
    pdf.start_section("8. Deployment")
    section_heading(pdf, "8. Deployment")

    section_heading(pdf, "Docker Compose Services", level=2)
    two_col_table(pdf,
        ["Service", "Image", "Port", "Description"],
        [
            ["virtual-wallet-db",
             "mariadb:11",
             "3306",
             "MariaDB database. Root password: root. DB name: virtual_wallet. "
             "Health-checked every 5 s (10 retries) via mariadb-admin ping."],
            ["virtual-wallet-backend",
             "Dockerfile (eclipse-temurin:17-jre-jammy)",
             "8080",
             "Spring Boot application. Depends on db health check. "
             "JAR: backend-0.0.1-SNAPSHOT.jar"],
        ],
        [36, 48, 14, 92],
    )

    section_heading(pdf, "Build & Run", level=2)
    code_block(pdf, [
        "# Step 1: Build the Spring Boot JAR",
        "cd backend",
        "./gradlew build",
        "",
        "# Step 2: Build Docker images and start all services",
        "docker compose up --build",
        "",
        "# Backend will be available at http://localhost:8080/api/v0",
        "# Database at localhost:3306 (virtual_wallet)",
    ])

    section_heading(pdf, "Environment Variables", level=2)
    body_text(pdf,
        "The docker-compose.yml passes environment variables that override "
        "application.properties values at runtime. Secrets are never committed to source control; "
        "they are loaded from application-secrets.properties locally or injected as env vars in CI/CD.")
    two_col_table(pdf,
        ["Variable", "Default / Source", "Description"],
        [
            ["SPRING_DATASOURCE_URL",      "jdbc:mariadb://db:3306/virtual_wallet", "DB connection URL (service name 'db' inside Compose network)"],
            ["SPRING_DATASOURCE_USERNAME", "root",                                   "Database username"],
            ["SPRING_DATASOURCE_PASSWORD", "root",                                   "Database password"],
            ["JWT_SECRET",                 "256-char hex string in properties",       "HMAC-SHA256 signing key"],
            ["EXCHANGE_RATE_API_KEY",       "application-secrets.properties",        "ExchangeRate-API v6 key"],
            ["SPRING_MAIL_USERNAME",        "application-secrets.properties",        "Gmail SMTP username"],
            ["SPRING_MAIL_PASSWORD",        "application-secrets.properties",        "Gmail App Password"],
        ],
        [54, 52, 84],
    )

    section_heading(pdf, "Frontend Production Build", level=2)
    code_block(pdf, [
        "cd frontend",
        "npm run build",
        "# Static files in frontend/dist/",
        "# Set VITE_API_BASE_URL env var before build to point at production backend",
    ])

    section_heading(pdf, "Health Check", level=2)
    body_text(pdf,
        "The MariaDB service exposes a Docker health check that runs "
        "mariadb-admin ping every 5 seconds with a 10-retry limit. "
        "The Spring Boot service is configured with depends_on: db: condition: service_healthy, "
        "so the backend will not start until the database is accepting connections. "
        "This prevents connection-refused errors on fresh container startup.")

    section_heading(pdf, "Key Configuration Properties", level=2)
    code_block(pdf, [
        "# src/main/resources/application.properties",
        "spring.datasource.url=jdbc:mariadb://localhost:3306/virtual_wallet",
        "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=true",
        "spring.jackson.property-naming-strategy=SNAKE_CASE",
        "jwt.secret=<256-char hex key>",
        "",
        "# src/main/resources/application-secrets.properties (NOT committed)",
        "exchange.rate.api.key=YOUR_KEY",
        "spring.mail.username=your@gmail.com",
        "spring.mail.password=your_app_password",
    ])


# -- TOC renderer -------------------------------------------------------------

def render_toc(pdf: Doc, outline) -> None:
    """Render the Table of Contents into the reserved placeholder pages."""
    pdf.set_font("Helvetica", "B", 16)
    pdf.set_text_color(*PRIMARY)
    pdf.cell(0, 12, "Table of Contents", align="C", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    # Blue rule
    pdf.set_fill_color(*PRIMARY)
    pdf.rect(pdf.l_margin, pdf.get_y(), 190, 1, "F")
    pdf.ln(6)

    for item in outline:
        if item.level == 0:
            pdf.set_font("Helvetica", "B", 11)
            pdf.set_text_color(*DARK)
            indent = 0
        else:
            pdf.set_font("Helvetica", "", 10)
            pdf.set_text_color(*MUTED)
            indent = 8

        link = pdf.add_link(page=item.page_number)
        # Title + dotted leader + page number
        title = item.name
        page_str = str(item.page_number)
        # Calculate available width for dots
        title_w = pdf.get_string_width(title) + indent
        page_w = pdf.get_string_width(page_str) + 4
        dot_w = 190 - title_w - page_w - 2
        dots = ""
        dot_char_w = pdf.get_string_width(".")
        if dot_char_w > 0:
            n_dots = max(0, int(dot_w / dot_char_w))
            dots = " " + ("." * n_dots) + " "

        pdf.set_x(pdf.l_margin + indent)
        pdf.cell(0, 7,
                 f"{title}{dots}{page_str}",
                 link=link,
                 new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        if item.level == 0:
            pdf.ln(1)

    pdf.set_text_color(*DARK)


# -- Main entry point ---------------------------------------------------------

def main() -> None:
    pdf = Doc("P", "mm", "A4")
    pdf.set_margins(left=10, top=10, right=10)
    pdf.set_auto_page_break(auto=True, margin=18)

    # Title page (no header/footer)
    pdf.add_page()
    render_title_page(pdf)

    # Reserve 2 pages for TOC -- back-filled after all content is rendered
    pdf.insert_toc_placeholder(render_toc, pages=2)

    render_overview(pdf)
    render_getting_started(pdf)
    render_auth(pdf)
    render_features(pdf)
    render_api(pdf)
    render_data_models(pdf)
    render_frontend(pdf)
    render_deployment(pdf)

    pdf.output(OUTPUT_PATH)
    print(f"PDF written to {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
