package net.runelite.client.plugins.oneclickadambankskills;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class Types
{
public enum Banks
{
NPC,
BOOTH,
CHEST,
}

public enum Skill
{
Use14on14,
Use1on27,
Humidify
}
}
