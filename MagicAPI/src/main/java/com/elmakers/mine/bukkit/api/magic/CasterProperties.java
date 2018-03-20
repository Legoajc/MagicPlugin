package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CasterProperties extends MagicConfigurable {
    boolean hasSpell(String spellKey);
    Collection<String> getSpells();
    boolean addSpell(String spellKey);
    Mage getMage();
    void removeMana(float mana);
    float getMana();
    int getManaMax();
    void setMana(float mana);
    void setManaMax(int manaMax);
    int getManaRegeneration();
    void setManaRegeneration(int manaRegeneration);
    ProgressionPath getPath();
    boolean canProgress();
    Double getAttribute(String attributeKey);
    void setAttribute(String attributeKey, Double attributeValue);
    boolean addItem(ItemStack item);

    /**
     * Returns a Spell for a given spell key, if this caster has the spell.
     *
     * Will return the correct spell level for the given spell, regarless of the level requested.
     *
     * @param spellKey The spell key to request
     * @return the Spell, or null if not present here.
     */
    @Nullable Spell getSpell(String spellKey);
}
