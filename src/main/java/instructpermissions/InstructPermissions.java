package instructpermissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import instructability.Instructables;
import instructability.permission.PermissionRegistry;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InstructPermissions implements IModule {

	private Path permissionsPath = Paths.get("permissions.json");
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	public boolean enable(IDiscordClient client) {
		try {
			if (!Files.exists(permissionsPath))
				savePermissions();

			PermissionRegistry reg = new Gson()
					.fromJson(Files.newBufferedReader(permissionsPath), PermissionRegistry.class);

			Instructables.setPermissionRegistry(reg);
			executor.scheduleWithFixedDelay(this::savePermissions, 60, 60, TimeUnit.SECONDS);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void disable() {
		savePermissions();
	}

	public void savePermissions() {
		try {
			Files.write(permissionsPath,
					new GsonBuilder()
							.setPrettyPrinting()
							.create()
							.toJson(Instructables.getPermissionRegistry())
							.getBytes(),
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

			Discord4J.LOGGER.debug("Saved permissions to {}", permissionsPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return getClass().getPackage().getImplementationTitle();
	}

	@Override
	public String getAuthor() {
		return "Kaioru";
	}

	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	@Override
	public String getMinimumDiscord4JVersion() {
		return "2.4.0";
	}

}
