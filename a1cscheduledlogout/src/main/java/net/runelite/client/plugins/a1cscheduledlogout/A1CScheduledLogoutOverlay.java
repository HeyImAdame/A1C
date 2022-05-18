package net.runelite.client.plugins.a1cscheduledlogout;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;
import java.awt.*;

@Singleton
public class A1CScheduledLogoutOverlay extends OverlayPanel {
    private final A1CScheduledLogoutPlugin plugin;
    private final A1CScheduledLogoutConfig config;

    @Inject
    private A1CScheduledLogoutOverlay(final A1CScheduledLogoutPlugin plugin, final A1CScheduledLogoutConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.overlay())
        {
            return null;
        }
        panelComponent.setBackgroundColor(Color.black);
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("LOGGING OUT IN: " + plugin.CountdownTimer)
                .build());
        return super.render(graphics);
    }
}
