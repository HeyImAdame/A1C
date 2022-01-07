/*
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.nex;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("nex")
public interface NexConfig extends Config
{
	@ConfigSection(
		name = "General",
		description = "Configure general settings.",
		position = 100,
		keyName = "generalSection"
	)
	String generalSection = "General";

	@ConfigItem(
		keyName = "flash",
		name = "Flash on null attack",
		description = "Flash screen when attacking and invulnerable",
		position = 101,
		section = generalSection
	)
	default boolean flash()
	{
		return false;
	}

	@ConfigItem(
		keyName = "prayerHelper",
		name = "Prayer helper",
		description = "Displays the correct prayer to use at various points in the fight.",
		position = 102,
		section = generalSection,
		enumClass = PrayerDisplay.class
	)
	default PrayerDisplay prayerHelper()
	{
		return PrayerDisplay.BOTH;
	}

	@ConfigItem(
		position = 103,
		keyName = "alwaysShowPrayerHelper",
		name = "Always Show Prayer Helper",
		description = "Render prayer helper at all time, even when other inventory tabs are open.",
		section = generalSection
	)
	default boolean alwaysShowPrayerHelper()
	{
		return true;
	}

	@ConfigItem(
		position = 104,
		keyName = "indicatePrayerIsCorrect",
		name = "Indicate when Correct Prayer",
		description = "Show a green box around prayer when its correct",
		section = generalSection
	)
	default boolean indicatePrayerIsCorrect()
	{
		return false;
	}

	@ConfigItem(
		position = 105,
		keyName = "indicateNexVulnerability",
		name = "Indicate Nex",
		description = "Indicate when you should or should not be attacking Nex",
		section = generalSection,
		enumClass = VulnerabilityDisplay.class

	)
	default VulnerabilityDisplay indicateNexVulnerability()
	{
		return VulnerabilityDisplay.INVULNERABLE;
	}

	@ConfigItem(
		position = 106,
		keyName = "indicateMinionVulnerability",
		name = "Indicate Minion",
		description = "Indicate when you should attack a minion",
		section = generalSection,
		enumClass = VulnerabilityDisplay.class
	)
	default VulnerabilityDisplay indicateMinionVulnerability()
	{
		return VulnerabilityDisplay.VULNERABLE;
	}

	@Alpha
	@ConfigItem(
		position = 107,
		keyName = "invulnerableColor",
		name = "Invulnerable Color",
		description = "Color for when something is invulnerable",
		section = generalSection
	)
	default Color invulnerableColor()
	{
		return new Color(241, 48, 48, 224);
	}

	@Range(
		min = 1,
		max = 10
	)
	@ConfigItem(
		position = 108,
		keyName = "invulnerableWidth",
		name = "Invulnerable Width",
		description = "Size for outline",
		section = generalSection
	)
	default int invulnerableWidth()
	{
		return 3;
	}

	@Alpha
	@ConfigItem(
		position = 109,
		keyName = "vulnerableColor",
		name = "Vulnerable Color",
		description = "Color for vulnerable things",
		section = generalSection
	)
	default Color vulnerableColor()
	{
		return new Color(61, 241, 48, 224);
	}

	@Range(
		min = 1,
		max = 10
	)
	@ConfigItem(
		position = 110,
		keyName = "vulnerableWidth",
		name = "Vulnerable Width",
		description = "Size for outline",
		section = generalSection
	)
	default int vulnerableWidth()
	{
		return 3;
	}

	@ConfigItem(
		position = 111,
		keyName = "indicateInvulnerableNexTicks",
		name = "Invulnerable Nex Ticks",
		description = "if we know how many ticks before shes clickable",
		hidden = true,
		unhide = "indicateInvulnerableNexTicks",
		section = generalSection
	)
	default boolean indicateInvulnerableNexTicks()
	{
		return true;
	}

	@Range(
		min = 6,
		max = 72
	)
	@ConfigItem(
		position = 112,
		keyName = "indicateInvulnerableNexTicksFontSize",
		name = "Ticks Font Size",
		description = "size of le font",
		hidden = true,
		unhide = "indicateInvulnerableNexTicks",
		section = generalSection
	)
	default int indicateInvulnerableNexTicksFontSize()
	{
		return 18;
	}

	@ConfigItem(
		position = 113,
		keyName = "drawNexHp",
		name = "Nex HP %",
		description = "Draw nex HP per phase",
		section = generalSection
	)
	default boolean drawNexHp()
	{
		return true;
	}

	@ConfigItem(
		position = 114,
		keyName = "drawMinionHP",
		name = "Minion HP %",
		description = "Draw minion HP",
		section = generalSection
	)
	default boolean drawMinionHP()
	{
		return true;
	}

	@Range(
		min = 6,
		max = 72
	)
	@ConfigItem(
		position = 115,
		keyName = "drawNexHpFontSize",
		name = "HP Font Size",
		description = "size of next hp",
		hidden = true,
		unhide = "drawNexHp",
		section = generalSection
	)
	default int drawNexHpFontSize()
	{
		return 12;
	}

	@ConfigSection(
		name = "Smoke Settings",
		description = "Configure settings for the virus",
		position = 200,
		keyName = "shadowSection"
	)
	String smokeSection = "Smoke Phase";

	@ConfigItem(
		keyName = "hideHealthyPlayers",
		name = "Hide healthy players",
		description = "Hides all players that are not coughing",
		position = 201,
		section = smokeSection
	)
	default boolean hideHealthyPlayers()
	{
		return false;
	}

	@Range(
		max = 60,
		min = 1
	)
	@ConfigItem(
		keyName = "hideHealthyAbove",
		name = "Hide above #",
		description = "Hides all players when more than set number are in room",
		position = 202,
		section = smokeSection,
		hidden = true,
		unhide = "hideHealthyPlayers"
	)
	default int hideAboveNumber()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "coughTileIndicator",
		name = "Cough tiles highlight",
		description = "Shows the tiles that will make you sick",
		position = 203,
		section = smokeSection
	)
	default boolean coughTileIndicator()
	{
		return true;
	}

	@Range(
		max = 20,
		min = 1
	)
	@ConfigItem(
		keyName = "coughTileRenderDistance",
		name = "Render Distance",
		description = "Render cough distance in tiles from your player",
		position = 204,
		section = smokeSection
	)
	@Units("tiles")
	default int coughTileRenderDistance()
	{
		return 8;
	}

	@Alpha
	@ConfigItem(
		keyName = "coughBorderColour",
		name = "Cough border colour",
		description = "Colour the edges of the area highlighted by sickness",
		position = 205,
		section = smokeSection
	)
	default Color coughBorderColour()
	{
		return new Color(255, 0, 98, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "coughColour",
		name = "Shadows colour",
		description = "Colour for sickness highlight",
		position = 206,
		section = smokeSection
	)
	default Color coughColour()
	{
		return new Color(255, 0, 98, 51);
	}

	@ConfigSection(
		name = "Shadow Phase",
		description = "Configure Shadow Phase settings.",
		position = 300,
		keyName = "shadowSection"
	)
	String shadowSection = "Shadow Phase";

	@ConfigItem(
		keyName = "shadowsIndicator",
		name = "Shadows highlight",
		description = "Shows the ticks until portals will damage you",
		position = 301,
		section = shadowSection
	)
	default boolean shadowsIndicator()
	{
		return true;
	}

	@Range(
		max = 20,
		min = 1
	)
	@ConfigItem(
		keyName = "shadowsRenderDistance",
		name = "Render Distance",
		description = "Render shadows distance in tiles from your player",
		position = 302,
		section = shadowSection
	)
	@Units("tiles")
	default int shadowsRenderDistance()
	{
		return 6;
	}


	@ConfigItem(
		keyName = "shadowsTickCounter",
		name = "Shadows Tick Counter",
		description = "Displays the number of ticks until shadows do damage",
		position = 303,
		section = shadowSection
	)
	default boolean shadowsTickCounter()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "shadowsBorderColour",
		name = "Shadows border colour",
		description = "Colour the edges of the area highlighted by shadows",
		position = 304,
		section = shadowSection
	)
	default Color shadowsBorderColour()
	{
		return new Color(0, 255, 255, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "shadowsColour",
		name = "Shadows colour",
		description = "Colour for shadows highlight",
		position = 305,
		section = shadowSection
	)
	default Color shadowsColour()
	{
		return new Color(0, 255, 255, 50);
	}


	enum PrayerDisplay
	{
		PRAYER_TAB,
		BOTTOM_RIGHT,
		BOTH;

		public boolean showInfoBox()
		{
			switch (this)
			{
				case BOTTOM_RIGHT:
				case BOTH:
					return true;
				default:
					return false;
			}
		}

		public boolean showWidgetHelper()
		{
			switch (this)
			{
				case PRAYER_TAB:
				case BOTH:
					return true;
				default:
					return false;
			}
		}
	}

	enum VulnerabilityDisplay
	{
		VULNERABLE,
		INVULNERABLE,
		BOTH,
		NONE;

		public boolean showVulnerable()
		{
			switch (this)
			{
				case BOTH:
				case VULNERABLE:
					return true;
				default:
					return false;
			}
		}

		public boolean showInvulnerable()
		{
			switch (this)
			{
				case BOTH:
				case INVULNERABLE:
					return true;
				default:
					return false;
			}
		}
	}
}