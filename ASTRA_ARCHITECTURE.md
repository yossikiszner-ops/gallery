# Astra / AI Edge Gallery architecture

This fork keeps Google AI Edge Gallery's on-device model experience and adds a modular universal Android agent without coupling the new engine to the visual design.

## Design rules

1. Local-first and privacy-first.
2. UI is replaceable; agent services must not depend on Compose screens.
3. Prefer structured Android APIs/Intents, then Accessibility, then Shizuku, with vision as a fallback.
4. Provider credentials never live in source, logs, prompts, Skills, or ordinary preferences.
5. Every powerful capability is independently permissioned.
6. Full autonomy is user-selectable, but system/security boundaries remain enforced.

## Modules

### Provider & Model Hub
Provider-neutral discovery and capability probing for Google Gemini, OpenRouter, NVIDIA NIM, OpenAI, Anthropic, Groq, Cerebras, Together, Mistral, DeepSeek, xAI, and custom OpenAI-compatible endpoints.

Flow: choose provider -> enter key -> securely store key -> discover models -> probe capabilities -> explain models -> user chooses Manual or Auto routing.

### Agent Core
Goal -> Observe -> Understand -> Plan -> Select model/tools -> Act -> Verify -> Recover -> Learn.

### Android action stack
1. Android APIs and Intents
2. Accessibility service
3. Shizuku-backed privileged actions when explicitly enabled
4. Screenshot/vision fallback

### Voice
Pluggable voice engines with a searchable voice catalog. Initial adapters: Android System TTS, Piper, Kokoro, Chatterbox Multilingual, Fish Speech. Heavy engines/models are optional downloads, not bundled into the base APK.

Wake word is optional and user-trained. STT, wake-word detection and TTS remain separate interfaces so engines can be replaced independently.

### Skills
Skills contain triggers, variables, actions, conditions, verification and recovery. The agent may propose a reusable Skill after successfully completing a repeatable workflow. Users can inspect, edit, disable and delete Skills.

### Training mode
The user starts training, demonstrates a workflow, names it, and the app converts relevant structured UI/actions into a parameterized Skill. Training should avoid recording unrelated sensitive content.

### MCP / Connectors
MCP tools use explicit per-connector permissions. Native connectors are preferred when reliable; UI automation remains the fallback for apps without integrations.

### Language
English and Hebrew/RTL. Technical product terms such as API, MCP, Model, Provider, Wake Word, Accessibility, Shizuku and Agent may remain in English in Hebrew UI.

## Planned phases

1. Foundation: providers, model discovery/probing, encrypted credentials, model router, Hebrew/RTL, voice registry.
2. Agent: loop, action abstraction, Accessibility, Shizuku, vision fallback.
3. Voice: STT/TTS adapters, catalog, downloads, wake-word enrollment, Digital Assistant integration.
4. Intelligence: Skills, Training mode, verification/recovery and model routing policies.
5. Ecosystem: MCP/connectors, granular permissions, activity/audit log.
6. Design: deep UI redesign as a separate project after the engine stabilizes.
