package Files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.MCDungeon.battleArena.Main;

public class AbstractFile {
	
	protected Main main;
	private File file;
	protected FileConfiguration config;
	
	public AbstractFile(Main main, String filename)
	{
		this.main = main;
		this.file = new File(main.getDataFolder(), filename);
		
		if(!file.exists())
		{
			try 
			{
				file.createNewFile();
				config.set("items", Material.DIAMOND_AXE.name());
				config.set("items", Material.DIAMOND_SWORD.name());
				config.set("items", Material.DIAMOND_HELMET.name());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}
	
	public void save() {
		try {
			config.save(file);
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
