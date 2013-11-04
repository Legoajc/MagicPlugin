package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class PlayerSpells 
{
	protected Player player;
	protected Spells master;
	protected HashMap<String, Spell> spells = new HashMap<String, Spell>();
	private Inventory							storedInventory  			   = null;
	private Wand								activeWand					   = null;
	private final List<Spell>                   movementListeners              = new ArrayList<Spell>();
	private final List<Spell>                   quitListeners                  = new ArrayList<Spell>();
	private final List<Spell>                   deathListeners                 = new ArrayList<Spell>();
	private final List<Spell>                   damageListeners                = new ArrayList<Spell>();
	
	private float costReduction = 0;
	private float damageReduction = 0;
	private float damageReductionPhysical = 0;
	private float damageReductionProjectiles = 0;
	private float damageReductionFalling = 0;
	private float damageReductionFire = 0;
	private float damageReductionExplosions = 0;
	
	private int xpRegeneration = 0;
	private int xpMax = 0;
	private int healthRegeneration = 0;
	private int hungerRegeneration = 0;
	
	private ItemStack buildingMaterial = null;

	public int getXpRegeneration() {
		return xpRegeneration;
	}

	public void setXPRegeneration(int xpRegeneration) {
		this.xpRegeneration = xpRegeneration;
	}

	public int getXPMax() {
		return xpMax;
	}

	public void setXPMax(int xpMax) {
		this.xpMax = xpMax;
	}

	public int getHealthRegeneration() {
		return healthRegeneration;
	}

	public void setHealthRegeneration(int healthRegeneration) {
		this.healthRegeneration = healthRegeneration;
	}

	public int getHungerRegeneration() {
		return hungerRegeneration;
	}

	public void setHungerRegeneration(int hungerRegeneration) {
		this.hungerRegeneration = hungerRegeneration;
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical;
	}

	public void setDamageReductionPhysical(float damageReductionPhysical) {
		this.damageReductionPhysical = damageReductionPhysical;
	}

	public float getCostReduction() {
		return costReduction;
	}

	public void setCostReduction(float costReduction) {
		this.costReduction = costReduction;
	}

	public float getDamageReduction() {
		return damageReduction;
	}

	public void setDamageReduction(float damageReduction) {
		this.damageReduction = damageReduction;
	}

	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles;
	}

	public void setDamageReductionProjectiles(float damageReductionProjectiles) {
		this.damageReductionProjectiles = damageReductionProjectiles;
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling;
	}

	public void setDamageReductionFalling(float damageReductionFalling) {
		this.damageReductionFalling = damageReductionFalling;
	}

	public float getDamageReductionFire() {
		return damageReductionFire;
	}

	public void setDamageReductionFire(float damageReductionFire) {
		this.damageReductionFire = damageReductionFire;
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions;
	}

	public void setDamageReductionExplosions(float damageReductionExplosions) {
		this.damageReductionExplosions = damageReductionExplosions;
	}

	public boolean hasStoredInventory() {
		return storedInventory != null;
	}

	public Inventory getStoredInventory() {
		return storedInventory;
	}

	public boolean addToStoredInventory(ItemStack item) {
		if (storedInventory == null) {
			return false;
		}

		HashMap<Integer, ItemStack> remainder = storedInventory.addItem(item);
		for (ItemStack remains : remainder.values()) {
			player.getWorld().dropItemNaturally(player.getLocation(), remains);
		}

		return true;
	}

	public boolean storeInventory(int keepSlot, ItemStack keepItem) {
		Inventory inventory = player.getInventory();
		if (storedInventory != null) {
			return false;
		}

		if (keepItem != null) {
			inventory.clear(keepSlot);
		}

		storedInventory = InventoryUtils.createInventory(null, inventory.getSize(), "Magic.Wand.StoredInventory");
		
		// Make sure we don't store any spells or magical materials, just in case
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (Wand.isSpell(contents[i])) {
				contents[i] = null;
			}
		}
		storedInventory.setContents(contents);
		inventory.clear();

		if (keepItem != null) {
			inventory.setItem(keepSlot, keepItem);
		}

		return true;
	}

	public boolean storeInventory() {
		return storeInventory(player.getInventory().getHeldItemSlot(), player.getInventory().getItemInHand());
	}

	public boolean restoreInventory() {
		return restoreInventory(player.getInventory().getHeldItemSlot(), player.getInventory().getItemInHand());
	}

	public boolean restoreInventory(int keepSlot, ItemStack keepItem) {
		if (storedInventory == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		inventory.setContents(storedInventory.getContents());
		storedInventory = null;

		if (keepItem != null) {
			ItemStack occupied = inventory.getItem(keepSlot);
			inventory.setItem(keepSlot, keepItem);
			if (occupied != null) {
				HashMap<Integer, ItemStack> remainder = inventory.addItem(occupied);
				for (ItemStack remains : remainder.values()) {
					player.getWorld().dropItemNaturally(player.getLocation(), remains);
				}
			}
		}

		return true;
	}

	public void registerEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
		case PLAYER_MOVE:
			if (!movementListeners.contains(spell))
				movementListeners.add(spell);
			break;
		case PLAYER_QUIT:
			if (!quitListeners.contains(spell))
				quitListeners.add(spell);
			break;
		case PLAYER_DAMAGE:
			if (!damageListeners.contains(spell))
				damageListeners.add(spell);
			break;
		case PLAYER_DEATH:
			if (!deathListeners.contains(spell))
				deathListeners.add(spell);
			break;
		}
	}

	public void setPlayer(Player player)
	{
		this.player = player;
	}

	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
		case PLAYER_MOVE:
			movementListeners.remove(spell);
			break;
		case PLAYER_DAMAGE:
			damageListeners.remove(spell);
			break;
		case PLAYER_QUIT:
			quitListeners.remove(spell);
			break;
		case PLAYER_DEATH:
			deathListeners.remove(spell);
			break;
		}
	}

	public PlayerSpells(Spells master, Player player)
	{
		this.master = master;
		this.player = player;
	}
	
	public Player getPlayer()
	{
		return player;
	}

	public boolean cancel()
	{
		boolean result = false;
		for (Spell spell : spells.values())
		{
			result = result || spell.cancel();
		}
		return result;
	}

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(quitListeners);
		for (Spell listener : active)
		{
			listener.onPlayerQuit(event);
		}
	}

	public void onPlayerMove(PlayerMoveEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(movementListeners);
		for (Spell listener : active)
		{
			listener.onPlayerMove(event);
		}
	}

	public void onPlayerDeath(EntityDeathEvent event)
	{
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(deathListeners);
		for (Spell listener : active)
		{
			if (player == listener.getPlayer())
			{
				listener.onPlayerDeath(event);
			}
		}
	}
	
	public void onPlayerDamage(EntityDamageEvent event)
	{
		// First check for damage reduction
		float reduction = damageReduction;
		switch (event.getCause()) {
			case CONTACT:
			case ENTITY_ATTACK:
				reduction += damageReductionPhysical;
				break;
			case PROJECTILE:
				reduction += damageReductionProjectiles;
				break;
			case FALL:
				reduction += damageReductionFalling;
				break;
			case FIRE:
			case FIRE_TICK:
			case LAVA:
				reduction += damageReductionFire;
				break;
			case BLOCK_EXPLOSION:
			case ENTITY_EXPLOSION:
				reduction += damageReductionExplosions;
			default:
				break;
		}
		
		if (reduction >= 1) {
			event.setCancelled(true);
			return;
		}
		
		if (reduction > 0) {
			int newDamage = (int)Math.floor((1.0f - reduction) * event.getDamage());
			if (newDamage == 0) newDamage = 1;
			event.setDamage(newDamage);
		}
		
		// Send on to any registered spells
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(damageListeners);
		for (Spell listener : active)
		{
			listener.onPlayerDamage(event);
		}
	}


	public Spell getSpell(Material material)
	{
		Spell spell = master.getSpell(material);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		return getSpell(spell.getKey());
	}

	public Spell getSpell(String name)
	{
		Spell spell = master.getSpell(name);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		Spell playerSpell = spells.get(spell.getKey());
		if (playerSpell == null)
		{
			playerSpell = (Spell)spell.clone();
			spells.put(spell.getKey(), playerSpell);
		}

		playerSpell.setPlayer(player);

		return playerSpell;
	}
	
	public Spells getMaster() {
		return master;
	}
	
	public Inventory getInventory() {
		return hasStoredInventory() ? getStoredInventory() : player.getInventory();
	}
	
	public Wand getActiveWand() {
		return activeWand;
	}
	
	public void setActiveWand(Wand activeWand) {
		this.activeWand = activeWand;
	}
	
	@SuppressWarnings("deprecation")
	public void setBuildingMaterial(Material material, byte data) {
		if (material == Wand.CopyMaterial) {
			buildingMaterial = null;
			return;
		}
		if (material == Wand.EraseMaterial) {
			material = Material.AIR;
		}
		buildingMaterial = new ItemStack(material, 1, (short)0, data);
	}
	
	public void clearBuildingMaterial() {
		buildingMaterial = null;
	}
	
	public ItemStack getBuildingMaterial() {
		return buildingMaterial;
	}
	
	public boolean hasBuildPermission(Location location) {
		return master.hasBuildPermission(player, location);
	}
	
	public boolean hasBuildPermission(Block block) {
		return master.hasBuildPermission(player, block);
	}
	
	public void onCast(SpellResult result) {
		switch(result) {
			case SUCCESS:
				// No sound on success
				break;
			case INSUFFICIENT_RESOURCES:
				player.playEffect(player.getLocation(), Effect.SMOKE,  null);
				playSound(Sound.NOTE_BASS, 1, 1);
				break;
			case COOLDOWN:
				player.playEffect(player.getLocation(), Effect.SMOKE,  null);
				playSound(Sound.NOTE_SNARE_DRUM, 1, 1);
				break;
			case NO_TARGET:
				playSound(Sound.NOTE_STICKS, 1, 1);
				break;
			case COST_FREE:
				break;
			default:
				player.playEffect(player.getLocation(), Effect.EXTINGUISH,  null);
				playSound(Sound.NOTE_BASS_DRUM, 1, 1);
		}
	}
	
	public void playSound(Sound sound, float volume, float pitch) {
		if (master.soundsEnabled()) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
}
