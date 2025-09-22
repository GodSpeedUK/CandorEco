# CandorEco

A comprehensive economy plugin for Minecraft servers with advanced features including earn mechanics, player freezing capabilities, and robust database management.

## Features

- 💰 **Complete Economy System** - Balance management with Vault integration
- ⏰ **Earn Command** - Players can earn money with configurable cooldowns
- 🧊 **Freeze System** - Admins can freeze players to restrict economic activities
- 💸 **Money Transfers** - Secure player-to-player transactions
- 🗄️ **MySQL Database** - Persistent storage with automatic schema migration
- 🔧 **Admin Tools** - Comprehensive economy management commands
- 🔄 **Offline Support** - Freeze/unfreeze players even when they're offline

## Installation

1. Download the latest `CandorEco-1.0-SNAPSHOT.jar` from the releases
2. Place the JAR file in your server's `plugins` folder
3. Install [Vault](https://dev.bukkit.org/projects/vault) plugin as a dependency
4. Configure your MySQL database in `config.yml`
5. Restart your server

The plugin will automatically create and migrate database tables as needed.

## Configuration

After first run, edit `plugins/CandorEco/config.yml`:

```yaml
database:
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "your_username"
  password: "your_password"

economy:
  earn:
    amount: 50.0      # Amount players earn per /earn command
    cooldown: 86400   # Cooldown in seconds (24 hours = 86400)
  
  starting_balance: 100.0  # New player starting balance

messages:
  # All plugin messages are configurable
  earn_success: "&aYou earned $%.2f!"
  earn_cooldown: "&cYou must wait %s before earning again!"
  # ... more message configurations
```

## Commands

### Player Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/balance` | Check your current balance | `candoreco.balance` | `/balance [player]` |
| `/bal` | Alias for `/balance` | `candoreco.balance` | `/bal [player]` |
| `/pay` | Send money to another player | `candoreco.pay` | `/pay <player> <amount>` |
| `/earn` | Earn money (with cooldown) | `candoreco.earn` | `/earn` |

### Admin Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/eco give` | Give money to a player | `candoreco.admin` | `/eco give <player> <amount>` |
| `/eco take` | Take money from a player | `candoreco.admin` | `/eco take <player> <amount>` |
| `/eco set` | Set a player's balance | `candoreco.admin` | `/eco set <player> <amount>` |
| `/eco reset` | Reset a player's balance | `candoreco.admin` | `/eco reset <player>` |
| `/eco freeze` | Freeze/unfreeze a player | `candoreco.admin.freeze` | `/eco freeze <player>` |

## Permissions

### Default Permissions (All Players)
- `candoreco.balance` - Check balances
- `candoreco.pay` - Send money to other players
- `candoreco.earn` - Use the earn command

### Admin Permissions
- `candoreco.admin` - Full economy management
- `candoreco.admin.freeze` - Freeze/unfreeze players
- `candoreco.balance.others` - Check other players' balances

### Permission Inheritance
```yaml
# Example permissions.yml setup
groups:
  default:
    permissions:
      - candoreco.balance
      - candoreco.pay
      - candoreco.earn
  
  admin:
    permissions:
      - candoreco.*
```

## Features in Detail

### Earn System
- Players can use `/earn` to receive money
- Configurable cooldown period (default: 24 hours)
- Cooldown persists across server restarts
- Shows remaining time when on cooldown

### Freeze System
- Admins can freeze players to restrict all economic activities
- Frozen players cannot:
  - Send or receive money via `/pay`
  - Use the `/earn` command
  - Participate in any economy transactions
- Works on both online and offline players
- Freeze status persists across server restarts

### Database Features
- **Automatic Migration**: Upgrades existing databases seamlessly
- **UUID-Based**: Uses player UUIDs instead of usernames
- **Thread-Safe**: All database operations are properly synchronized
- **Error Handling**: Graceful degradation if database issues occur

### Vault Integration
- Full compatibility with other economy plugins
- Provides economy service to other plugins
- Thread-safe registration with proper Bukkit scheduling

## Database Schema

The plugin creates the following table structure:

```sql
CREATE TABLE player_accounts (
    uuid CHAR(36) PRIMARY KEY,
    balance DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    frozen BOOLEAN NOT NULL DEFAULT FALSE,
    last_earn BIGINT NOT NULL DEFAULT 0
);
```

## Command Examples

### Player Usage
```
/balance                    # Check your balance
/balance Steve             # Check Steve's balance (with permission)
/pay Steve 100             # Send $100 to Steve
/earn                      # Earn money (if cooldown expired)
```

### Admin Usage
```
/eco give Steve 500        # Give Steve $500
/eco take Steve 100        # Take $100 from Steve
/eco set Steve 1000        # Set Steve's balance to $1000
/eco reset Steve           # Reset Steve's balance to starting amount
/eco freeze Steve          # Freeze Steve's economic activities
/eco freeze Steve          # Unfreeze Steve (toggle command)
```

## Error Handling

### Common Issues and Solutions

**Database Connection Failed**
- Check MySQL credentials in config.yml
- Ensure MySQL server is running
- Verify database exists and user has proper permissions

**"Unknown column 'frozen'" Error**
- This is automatically resolved by the plugin's migration system
- If persisting, check MySQL user has ALTER privileges

**Vault Not Found**
- Install the Vault plugin
- Ensure Vault loads before CandorEco

**Commands Not Working**
- Check permissions are properly assigned
- Verify player has required permission nodes
- Check console for error messages

## Development

### Building from Source
```bash
git clone <repository-url>
cd CandorEco
mvn clean install
```

### Dependencies
- Java 8+
- Bukkit/Spigot/Paper API
- Vault (runtime dependency)
- MySQL 5.7+ or MariaDB 10.2+

## Support

For issues, feature requests, or support:
- Check the console logs for error details
- Verify all dependencies are installed
- Ensure proper permissions are configured
- Review database connectivity

## License

This project is licensed under the MIT License.

## Version History

### v1.0.0
- Initial release
- Complete economy system
- Earn command with cooldowns
- Freeze system for admin control
- Automatic database migration
- Full Vault integration