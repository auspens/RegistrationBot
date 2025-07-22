
# EventRegistration Telegram Bot

A Kotlin-based Telegram bot for event registration and management, built with PostgreSQL database support and featuring admin broadcasting capabilities.

![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![PostgreSQL](https://img.shields.io/badge/postgresql-supported-blue.svg)
![Telegram](https://img.shields.io/badge/telegram-bot-blue.svg)

## 🚀 Features

### For Users 👥
- 📅 **Event Registration**: Register for events through deep links
- ❌ **Cancel Registration**: Ability to cancel event registration
- 📞 **Contact Organizers**: Direct communication with event organizers
- 📸 **Event Details**: View event descriptions and photos

### For Admins 👑
- 🎯 **Event Management**: Create, modify, and delete events
- 📊 **User Management**: View and export user data
- 📢 **Broadcasting**: Send messages to all users or event participants
- 🔗 **Deep Links**: Generate registration links for events
- 📈 **Analytics**: Track event participation and user statistics

## 🛠️ Technology Stack

- **Language**: Kotlin
- **Database**: PostgreSQL (with local file storage option)
- **Framework**: kotlin-telegram-bot
- **ORM**: Exposed (Jetbrains)
- **Build Tool**: Gradle

## 📁 Project Structure

```
src/main/kotlin/org/example/
├── bot/
│   ├── RegistrationBot.kt              # Main bot configuration
│   └── handlers/
│       ├── AdminHandler.kt         # Admin-specific functionality
│       ├── UserHandler.kt          # User-specific functionality
│       └── CommonHandler.kt        # Shared command handlers
├── models/                         # Data models
├── repository/                     # Data access layer
└── Main.kt                        # Application entry point
```

## ⚙️ Setup and Installation

### Prerequisites 📋

- Java 11 or higher ☕
- PostgreSQL database (optional - can use local file storage) 🐘
- Telegram Bot Token (obtain from [@BotFather](https://t.me/BotFather)) 🤖

### Environment Variables 🔧

Create a `.env` file or set the following environment variables:

```bash
# Required
BOT_TOKEN=your_telegram_bot_token_here
BOT_ANSWER_TIMEOUT=30

# Database (optional - comment out for local file storage)
DB_URL=jdbc:postgresql://localhost:5432/[name of the db]
DB_USER=your_db_username
DB_PASSWORD=your_db_password

# Admin contact information
ADMIN_CONTACT=@your_admin_username
```

### Installation Steps 🚀

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/registration-telegram-bot.git
   cd registration-telegram-bot
   ```

2. **Set up PostgreSQL** (optional)
   ```sql
   CREATE DATABASE regbot;
   ```

3. **Configure environment variables**
   ```bash
   export BOT_TOKEN="your_bot_token"
   export BOT_ANSWER_TIMEOUT="30"
   export ADMIN_CONTACT="@your_username"
   # Add database variables if using PostgreSQL
   ```

4. **Build and run**
   ```bash
   ./gradlew build
   ./gradlew run
   ```

## 💾 Database Configuration

The bot supports two storage options:

### PostgreSQL (Recommended) 🐘
Uncomment the database connection lines in `Main.kt`:
```kotlin
Database.connect(
    url = System.getenv("DB_URL"),
    driver = "org.postgresql.Driver",
    user = System.getenv("DB_USER"),
    password = System.getenv("DB_PASSWORD")
)
```

### Local File Storage 📂
Keep the database connection lines commented out for local file-based storage.

## 📖 Usage

### Admin Commands 👑

1. **Start the bot**: `/start`
2. **Create Event**: Use the inline keyboard to create new events
3. **Manage Events**: View, edit, or delete existing events
4. **User Management**: Export user data and send broadcasts
5. **Broadcasting**: Send messages to all users or specific event participants

### User Commands 👥

1. **Register for Event**: Click registration link or use deep link
2. **View Event Details**: See event description and photos
3. **Cancel Registration**: Remove yourself from an event
4. **Contact Organizers**: Get help from event organizers

### Deep Links 🔗

Events can be accessed via deep links in the format:
```
https://t.me/your_bot_username?start=EVENT_ID
```

## 🔐 Admin Configuration

To configure admin users, modify the `isAdmin()` function in your utils package to include authorized user IDs:

```kotlin
fun isAdmin(userId: Long): Boolean {
    val adminIds = listOf(123456789L, 987654321L) // Replace with actual admin IDs
    return userId in adminIds
}
```

## 📋 Event Management Workflow

1. **Create Event**: Admin creates event with name, description, photo, and participant limit
2. **Generate Link**: System generates deep link for registration
3. **Share Link**: Distribute registration link to potential participants
4. **User Registration**: Users click link and register for event
5. **Management**: Admin can modify event details, view participants, send updates

## ⚠️ Error Handling

The bot includes comprehensive error handling for:
- Invalid event IDs
- Full event capacity
- Database connection issues
- Invalid user permissions
- Missing environment variables

## 🔒 Security Features

- **Admin-only Functions**: Sensitive operations restricted to admin users
- **User Data Protection**: User information handled securely
- **Input Validation**: All user inputs validated before processing
- **Error Logging**: Comprehensive logging for debugging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the GitHub repository
- Contact the maintainers via Telegram
- Check the documentation and code comments

## 🗺️ Roadmap

- [ ] Add event scheduling capabilities
- [ ] Implement user analytics dashboard
- [ ] Add multi-language support
- [ ] Integrate with external calendar systems
- [ ] Add event reminder notifications
- [ ] Implement user feedback system

## 🙏 Acknowledgments

- [kotlin-telegram-bot](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot) library
- [Exposed ORM](https://github.com/JetBrains/Exposed) by JetBrains
- Telegram Bot API documentation

---

**Note**: Make sure to keep your bot token and database credentials secure. Never commit sensitive information to version control.
