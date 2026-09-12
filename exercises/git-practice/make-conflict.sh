#!/usr/bin/env bash
# Creates a practice Git repository with a real merge conflict, for the Codebook lesson
# "Merge Conflicts Without Panic". Usage (from Git Bash, macOS, or Linux):
#   bash git-practice/make-conflict.sh [folder-name]
set -euo pipefail

dir="${1:-git-conflict-practice}"
if [ -e "$dir" ]; then
  echo "Folder '$dir' already exists. Delete it or pass a different folder name." >&2
  exit 1
fi

mkdir "$dir"
cd "$dir"
git init -q -b main
git config user.name "Codebook Practice"
git config user.email "practice@example.invalid"

cat > TurretConstants.java <<'EOF'
public class TurretConstants {
    public static final double turretOffsetChange = 0.05; // radians per operator bumper press
    public static final double flywheelTolerance = 1000;  // RPM below setpoint that still counts as ready
}
EOF
git add TurretConstants.java
git commit -q -m "Add turret constants"

git switch -q -c operator-trim
sed -i.bak 's/turretOffsetChange = 0.05/turretOffsetChange = 0.02/' TurretConstants.java
rm TurretConstants.java.bak
git commit -q -am "Make operator trim finer (0.02 rad per press)"

git switch -q main
sed -i.bak 's/turretOffsetChange = 0.05/turretOffsetChange = 0.10/' TurretConstants.java
rm TurretConstants.java.bak
git commit -q -am "Make operator trim coarser for fast corrections (0.10 rad per press)"

echo "Practice repository ready in '$dir'."
echo "Next:"
echo "  cd $dir"
echo "  git merge operator-trim"
