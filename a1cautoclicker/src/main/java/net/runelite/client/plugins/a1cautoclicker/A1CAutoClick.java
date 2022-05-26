/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.a1cautoclicker;
import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "A1C AutoClicker",
	description = "The better clicks",
	tags = "Adam",
	enabledByDefault = false
)
@Slf4j
public class A1CAutoClick extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private A1CAutoClickConfig config;

	@Inject
	private A1CAutoClickerOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	private ExecutorService executorService;
	private Point point;
	private Random random;
	private boolean run;
	private int ticksLogged;
	private int lastTick;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean flash;

	@Provides
	A1CAutoClickConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(A1CAutoClickConfig.class);
	}

	@Override
	protected void startUp()
	{//
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
		executorService = Executors.newSingleThreadExecutor();
		random = new Random();
	}

	@Override
	protected void shutDown()
	{
		run = false;
		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(hotkeyListener);
		executorService.shutdown();
		random = null;
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggle())
	{
		@Override
		public void hotkeyPressed()
		{
			run = !run;

			if (!run)
			{
				return;
			}
			point = client.getMouseCanvasPosition();

			executorService.submit(() ->
			{
				while (run)
				{
					if (client.getGameState() == GameState.LOGIN_SCREEN)
					{
						run = false;
						break;
					}
					if (client.getGameState() != GameState.LOGGED_IN)
					{
						if (ticksLogged > 10)
						{
							run = false;
							break;
						}
						ticksLogged = ticksLogged + client.getTickCount() - lastTick;
					}
					else
					{
						lastTick = client.getTickCount();
						ticksLogged = 0;
					}

					if (checkHitpoints() || checkInventory())
					{
						run = false;
						if (config.flash())
						{
							setFlash(true);
						}
						break;
					}

					click(point);

					try
					{
						Thread.sleep(randomDelay());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	};
	public void click(Rectangle rectangle)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		click(point);
	}
	public void click(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(501, point);
			mouseEvent(502, point);
			mouseEvent(500, point);
			return;
		}
		mouseEvent(501, p);
		mouseEvent(502, p);
		mouseEvent(500, p);
	}

	public Point getClickPoint(Rectangle rect)
	{
		final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}
	public int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	private void mouseEvent(int id, Point point)
	{
		MouseEvent e = new MouseEvent(
				client.getCanvas(), id,
				System.currentTimeMillis(),
				0, point.getX(), point.getY(),
				1, false, 1
		);

		client.getCanvas().dispatchEvent(e);
	}
	/**
	 * Generate a gaussian random (average at 0.0, std dev of 1.0)
	 * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
	 * get the log base e of it to make it shifted towards the right side
	 * invert it to shift the distribution to the other end
	 * clamp it to min max, any values outside of range are set to min or max
	 */
	private long randomDelay()
	{
		if (config.weightedDistribution())
		{
			return (long) clamp(
				(-Math.log(Math.abs(random.nextGaussian()))) * config.deviation() + config.target()
			);
		}
		else
		{
			/* generate a normal even distribution random */
			return (long) clamp(
				Math.round(random.nextGaussian() * config.deviation() + config.target())
			);
		}
	}

	private double clamp(double val)
	{
		return Math.max(config.min(), Math.min(config.max(), val));
	}

	private boolean checkHitpoints()
	{
		if (!config.autoDisableHp())
		{
			return false;
		}
		return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpThreshold();
	}

	private boolean checkInventory()
	{
		if (!config.autoDisableInv())
		{
			return false;
		}
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget == null)
		{
			return false;
		}
		return inventoryWidget.getDynamicChildren().length == 27;
	}
}
