// ====== LAUNCHER CONFIG ====== //
var config = {
	dir: "fastcraft", // Launcher directory
	title: "Nuark.xyz", // Window title
	icons: [ "favicon.png" ], // Window icon paths

	// Auth config
	newsURL: "http://fastcraft.ru/news.php", // News WebView URL
	linkText: "Забыли пароль?", // Text for link under "Auth" button
	linkURL: new java.net.URL("http://fastcraft.ru/account/security"), // Ссылка на востановленияе пароля
	regURL: new java.net.URL("http://fastcraft.ru/register/vk?reg=1"), // Ссылка на регистрацию
	lkURL: new java.net.URL("http://fastcraft.ru/pages/lk/"), // Ссылка на личный кабинет
	statusURL: new java.net.URL("http://fastcraft.ru/pages/price/"), // Ссылка на статусы
	
	// Settings defaults
	settingsMagic: 0xC0DE5, // Ancient magic, don't touch
	autoEnterDefault: true, // Should autoEnter be enabled by default?
	fullScreenDefault: false, // Should fullScreen be enabled by default?
	shadersDefault: false, // Shaders be enabled by default?
	opisDefault: false, // Opis be enabled by default?
	ramDefault: 1024, // Default RAM amount (0 for auto)

	// Custom JRE config (!!! DON'T CHANGE !!!)
	jvmMustdie32Dir: "jre-8u92-win32", jvmMustdie64Dir: "jre-8u92-win64",
	jvmLinux32Dir: "jre-8u92-linux32", jvmLinux64Dir: "jre-8u92-linux64",
	jvmMacOSXDir: "jre-8u92-macosx", jvmUnknownDir: "jre-8u92-unknown"
};

// ====== DON'T TOUCH! ====== //
var dir = IOHelper.HOME_DIR.resolve(config.dir);
if (!IOHelper.isDir(dir)) {
	java.nio.file.Files.createDirectory(dir);
}
var defaultUpdatesDir = dir.resolve("updates");
if (!IOHelper.isDir(defaultUpdatesDir)) {
	java.nio.file.Files.createDirectory(defaultUpdatesDir);
}
