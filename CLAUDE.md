# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sumatra is the central AI software for TIGERs Mannheim RoboCup SSL (Small Size League) team. It processes SSL-vision data, makes tactical decisions, and controls robot behaviors using a modular Java architecture.

## Essential Commands

### Build and Run
- `./gradlew build` - Full build with tests
- `./gradlew build -x test` - Build without tests
- `./gradlew :run` - Start Sumatra application
- `./gradlew :run --args="--help"` - Run with custom arguments
- `./gradlew installDist` - Create distribution in `build/install/sumatra`

### Testing
- `./gradlew test` - Run unit tests (excludes integration tests)
- `./gradlew integrationTest` - Run integration tests (where available)
- Tests use JUnit 5, AssertJ, and Mockito

### Code Quality
- `./gradlew jacocoTestReport` - Generate test coverage reports
- SonarQube integration available for code quality analysis

## Architecture Overview

### Modular System
- **49 modules** organized by functionality with XML-based dependency injection
- Configuration files in `config/moduli/` define which modules to load:
  - `sim.xml` - Internal simulation
  - `robocup.xml` - Competition setup
  - `simulation_protocol.xml` - External simulator integration

### Key Module Categories
- **Core**: `common`, `common-math`, `common-gui`, `sumatra-model`
- **AI/Decision Making**: `moduli-ai`, `sumatra-skillsystem`, `sumatra-pathfinder`
- **Communication**: `moduli-cam` (vision), `moduli-referee`, `moduli-botmanager`
- **Simulation**: `moduli-simulation`, `moduli-vision-simulation`
- **GUI**: `sumatra-gui-*` modules for different views

### Data Flow
```
SSL-Vision → Vision Filter → World Predictor → AI Agent → Skill System → Bot Manager → Robots
```

### Key Patterns
- **MVP (Model-View-Presenter)** for GUI components
  - Views should only contain UI structure, no logic. Note: This is currently not fully adhered to.
  - Only Presenters should use SwingUtils.invokeLater() for UI updates. Views (panels) should not.
- **Observer Pattern** for event-driven communication
- **Custom Module System** for dependency injection and loose coupling

## Development Notes

### Java Requirements
- **Java 21 LTS** (defined in `sumatra.java.gradle`)
- **Lombok** required - install IDE plugin for proper code generation support

### Configuration System
- XML-based module configuration with dependency resolution
- User-specific configs in `user.xml`
- Geometry definitions in `config/geometry/`

### Testing Strategy
- Unit tests exclude `**/*IntegrationTest.class` by default
- Integration tests in separate `integrationTest` source set
- Spock (Groovy) tests for some components
- Coverage reports exclude generated protobuf code

### Simulation Modes
- **Internal Simulator**: No external dependencies, AI vs AI
- **External Simulator**: SSL-Simulation-Protocol (grSim, ER-Force)
- **Multi-instance**: Connect multiple Sumatra instances for AI vs AI

### Coding Conventions
- Follow Java conventions (camelCase, PascalCase for classes)
- Use Lombok annotations for boilerplate code reduction
- Use method references and lambda expressions where applicable

## Entry Points

- **Main Class**: `edu.tigers.sumatra.Sumatra`
- **GUI Bootstrap**: Look for `MainPresenter` classes in GUI modules
- **Module Loading**: Dynamic loading based on XML configuration in `config/moduli/`

## Important Directories

- `config/` - All configuration files (geometry, modules, user settings)
- `modules/` - Individual module implementations
- `doc/` - AsciiDoc documentation including developer guide
- `buildSrc/` - Custom Gradle build logic and conventions