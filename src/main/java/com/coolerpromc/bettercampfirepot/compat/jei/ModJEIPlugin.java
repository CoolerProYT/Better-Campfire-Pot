package com.coolerpromc.bettercampfirepot.compat.jei;

import com.cobblemon.mod.common.integration.jei.cooking.CampfirePotRecipeCategory;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.menu.CookingPotScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ModJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return BetterCampfirePot.id("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(CampfirePotRecipeCategory.Companion.getRECIPE_TYPE(),  BetterCampfirePot.CAMPFIRE_POT_ITEMS.toArray(new ItemLike[1]));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CookingPotScreen.class, 96, 39, 22, 12, CampfirePotRecipeCategory.Companion.getRECIPE_TYPE());
        registration.addGuiContainerHandler(CookingPotScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(CookingPotScreen screen) {
                List<Rect2i> areas = new ArrayList<>();
                areas.add(new Rect2i(screen.getGuiLeft() + screen.getXSize() - 1, screen.getGuiTop() + 20, 30, 30));
                areas.add(new Rect2i(screen.getGuiLeft() - 29, screen.getGuiTop() + 20, 30, 52));
                if (screen.isConfigOpened()){
                    areas.add(new Rect2i(screen.getGuiLeft() + screen.getXSize() + 4, screen.getGuiTop() + 50, 30, 30));
                }
                return areas;
            }
        });
    }
}
