package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class CoxBankPanelOverlay extends Overlay
{
    private static final int CELL_SIZE = 36;
    private static final int COLUMNS = 6;
    private static final int PADDING = 10;

    private final CoxBankPlugin plugin;
    private final ItemManager itemManager;

    @Inject
    public CoxBankPanelOverlay(CoxBankPlugin plugin, ItemManager itemManager)
    {
        this.plugin = plugin;
        this.itemManager = itemManager;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.TOP_LEFT);
        setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isRaidStarted() || !plugin.isBankOpen())
        {
            return null;
        }

        List<Item> rawItems = plugin.getPrivateStorage();

        // Group by item ID and sum quantities
        Map<Integer, Integer> grouped = new LinkedHashMap<>();
        for (Item item : rawItems)
        {
            grouped.merge(item.getId(), item.getQuantity(), Integer::sum);
        }

        List<Map.Entry<Integer, Integer>> items = new ArrayList<>(grouped.entrySet());

        int rows = Math.max(1, (items.size() + COLUMNS - 1) / COLUMNS);
        int width = PADDING * 2 + COLUMNS * CELL_SIZE;
        int titleHeight = 30;
        int height = titleHeight + PADDING + rows * CELL_SIZE + PADDING;

        graphics.setColor(new Color(30, 30, 30, 240));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, 0, width, height);
        graphics.drawString("CoX Storage", PADDING, 20);

        int x = PADDING;
        int y = titleHeight + PADDING;

        for (int i = 0; i < items.size(); i++)
        {
            int itemId = items.get(i).getKey();
            int quantity = items.get(i).getValue();

            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int cellX = x + col * CELL_SIZE;
            int cellY = y + row * CELL_SIZE;

            BufferedImage image = itemManager.getImage(itemId, quantity, quantity > 1);
            if (image != null)
            {
                graphics.drawImage(image, cellX, cellY, null);
            }
        }

        return new Dimension(width, height);
    }
}