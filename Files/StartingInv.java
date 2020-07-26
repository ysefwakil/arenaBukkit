package Files;

import org.bukkit.Material;

import com.MCDungeon.battleArena.Main;

public class StartingInv extends AbstractFile {
	
	public StartingInv(Main main)
	{
		super(main, "startinginv.yml");
	}

	public void properInventory()
	{
		config.set("items", Material.DIAMOND_SWORD.name());
	}
	
}
