package org.hibernate.benchmark.enhancement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.hibernate.bytecode.enhance.spi.DefaultEnhancementContext;
import org.hibernate.bytecode.enhance.spi.Enhancer;
import org.hibernate.bytecode.enhance.spi.UnloadedClass;
import org.hibernate.bytecode.enhance.spi.UnloadedField;

import static org.hibernate.bytecode.internal.BytecodeProviderInitiator.buildDefaultBytecodeProvider;

/**
 * @author Marco Belladelli
 */
public class EnhancementUtils {
	private static class EnhancingClassLoader extends ClassLoader {
		private final Enhancer enhancer;
		private final Collection<String> classesToEnhance;

		public EnhancingClassLoader(Enhancer enhancer, Collection<String> classesToEnhance) {
			this.enhancer = enhancer;
			this.classesToEnhance = classesToEnhance;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if ( classesToEnhance.contains( name ) ) {
				Class<?> c = findLoadedClass( name );
				if ( c != null ) {
					return c;
				}

				try (InputStream is = getResourceAsStream( name.replace( '.', '/' ) + ".class" )) {
					if ( is == null ) {
						throw new ClassNotFoundException( name + " not found" );
					}

					byte[] original = new byte[is.available()];
					try (BufferedInputStream bis = new BufferedInputStream( is )) {
						bis.read( original );
					}

					byte[] enhanced = enhancer.enhance( name, original );
					if ( enhanced == null ) {
						return defineClass( name, original, 0, original.length );
					}
					Path f = Files.createTempDirectory( "" ).getParent()
							.resolve( name.replace( ".", File.separator ) + ".class" );
					Files.createDirectories( f.getParent() );
					try (OutputStream out = Files.newOutputStream( f )) {
						out.write( enhanced );
					}
					return defineClass( name, enhanced, 0, enhanced.length );
				}
				catch (Exception t) {
					throw new ClassNotFoundException( name + " not found", t );
				}
			}

			return getParent().loadClass( name );
		}
	}

	static class EnhancerTestContext extends DefaultEnhancementContext {
		@Override
		public boolean doBiDirectionalAssociationManagement(UnloadedField field) {
			return true;
		}

		@Override
		public boolean doDirtyCheckingInline(UnloadedClass classDescriptor) {
			return true;
		}

		@Override
		public boolean doExtendedEnhancement(UnloadedClass classDescriptor) {
			return true;
		}

		@Override
		public boolean hasLazyLoadableAttributes(UnloadedClass classDescriptor) {
			return true;
		}

		@Override
		public boolean isLazyLoadable(UnloadedField field) {
			return true;
		}
	}

	public static ClassLoader buildEnhancerClassLoader(Collection<String> classesToEnhance) {
		final EnhancerTestContext enhancerContext = new EnhancerTestContext();
		return new EnhancingClassLoader(
				buildDefaultBytecodeProvider().getEnhancer( enhancerContext ),
				classesToEnhance
		);
	}
}
