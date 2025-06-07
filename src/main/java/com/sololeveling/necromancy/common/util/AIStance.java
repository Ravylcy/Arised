package com.sololeveling.necromancy.common.util;

public enum AIStance {
    PASSIVE,    // Follows, does not attack
    DEFENSIVE,  // Follows, only attacks if owner or another shadow is hit
    AGGRESSIVE  // Follows, attacks any hostile mob in range
}