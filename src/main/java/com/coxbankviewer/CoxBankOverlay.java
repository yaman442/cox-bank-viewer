package com.coxbankviewer;

import java.awt.image.BufferedImage;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;

public class CoxBankOverlay extends Overlay implements MouseListener
{
    private final CoxBankPlugin plugin;
    private final MouseManager mouseManager;

    private final BufferedImage chestIcon;
    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 50;

    @Inject
    public CoxBankOverlay(CoxBankPlugin plugin, MouseManager mouseManager)
    {
        this.plugin = plugin;
        this.mouseManager = mouseManager;
        chestIcon = ImageUtil.loadImageResource(getClass(), "chest_icon.png");

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.TOP_LEFT);
        setMovable(true);
        setResizable(true);
    }

    public void registerMouseListener()
    {
        mouseManager.registerMouseListener(this);
    }

    public void unregisterMouseListener()
    {
        mouseManager.unregisterMouseListener(this);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isRaidStarted())
        {
            return null;
        }

        Dimension preferred = getPreferredSize();
        int width = preferred != null ? preferred.width : BUTTON_WIDTH;
        int height = preferred != null ? preferred.height : BUTTON_HEIGHT;

        if (chestIcon != null)
        {
            graphics.drawImage(chestIcon, 0, 0, width, height, null);
        }

        return new Dimension(width, height);
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent mouseEvent)
    {
        if (plugin.isRaidStarted() && getBounds().contains(mouseEvent.getPoint()))
        {
            plugin.toggleBank();
            mouseEvent.consume();
        }
        return mouseEvent;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent mouseEvent)
    {
        if (plugin.isRaidStarted() && getBounds().contains(mouseEvent.getPoint()))
        {
            mouseEvent.consume();
        }
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent mouseEvent)
    {
        if (plugin.isRaidStarted() && getBounds().contains(mouseEvent.getPoint()))
        {
            mouseEvent.consume();
        }
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent mouseEvent) { return mouseEvent; }
    @Override
    public MouseEvent mouseExited(MouseEvent mouseEvent) { return mouseEvent; }
    @Override
    public MouseEvent mouseDragged(MouseEvent mouseEvent) { return mouseEvent; }
    @Override
    public MouseEvent mouseMoved(MouseEvent mouseEvent) { return mouseEvent; }
}