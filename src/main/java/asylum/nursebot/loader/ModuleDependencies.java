package asylum.nursebot.loader;

import java.util.HashMap;

import asylum.nursebot.objects.Module;

public class ModuleDependencies extends HashMap<Class<? extends Module>, Module>{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public <C extends Module> C get(Class<C> clazz) {
		Module m = super.get(clazz);
		if (!(clazz.isInstance(m)))
			throw new IllegalArgumentException();
		return (C) m;
	}
}
