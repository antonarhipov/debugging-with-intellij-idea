#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/../.."

if [ ! -x /usr/lib/jvm/java-21-openjdk-amd64/bin/javac ]; then
    echo 'Installing Java 21'
    sudo apt-get update
    sudo apt-get install -y openjdk-21-jdk-headless
fi
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mkdir -p "$HOME/.config/air"
cat > "$HOME/.config/air/java.sh" <<'JAVA'
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
JAVA
profile="$HOME/.profile"
for candidate in "$HOME/.bash_profile" "$HOME/.bash_login" "$HOME/.profile"; do
    if [ -f "$candidate" ]; then profile="$candidate"; break; fi
done
for rc in "$profile" "$HOME/.bashrc"; do
    touch "$rc"
    if ! grep -q '# air-java-environment' "$rc"; then
        echo '. "$HOME/.config/air/java.sh" # air-java-environment' >> "$rc"
    fi
done

healthcheck() {
    echo 'Checking Java compilation and warming Gradle dependency caches'
    if ! bash gradlew --no-daemon testClasses; then
        echo 'Build failed. Ensure org.example:legacy-lib:1.0-SNAPSHOT is installed in the local Maven repository.' >&2
        return 1
    fi
}

if [ "${AIR_STARTUP_MODE:-}" = warmup ]; then
    healthcheck
fi
