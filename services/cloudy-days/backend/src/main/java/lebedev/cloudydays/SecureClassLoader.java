package lebedev.cloudydays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(SecureClassLoader.class);

    private static final String[] ALLOWED = new String[]{"java/lang", "lebedev/cloudydays/api"};
    private static final String[] DENIED = new String[]{"java/lang/reflect", "java/lang/ClassLoader", "java/lang/Thread", "java/lang/System", "java/lang/SecurityManager", "java/lang/Runtime", "java/lang/ProcessBuilder", "java/lang/Process"};

    private final URL jarUrl;

    public SecureClassLoader(URL jarUrl, ClassLoader parent) {
        super(parent);
        this.jarUrl = jarUrl;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        String resourcePath = name.replace('.', '/').concat(".class");
        URL classResource;
        try {
            classResource = new URL("jar:" + this.jarUrl + "!/" + resourcePath);
        } catch (MalformedURLException e) {
            throw new ClassNotFoundException("Malformed url: " + this.jarUrl + "!" + resourcePath);
        }

        byte[] data;
        URLConnection connection;
        try {
            connection = classResource.openConnection();
        } catch (IOException e) {
            throw new ClassNotFoundException("Error connecting to " + classResource, e);
        }
        connection.setUseCaches(false);
        try (InputStream classStream = connection.getInputStream()) {
            data = this.readBytes(classStream);
        } catch (IOException e) {
            throw new ClassNotFoundException("Error while reading class " + classResource, e);
        }

        ClassReader classReader = new ClassReader(data);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 8);
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn.getType() == 6) {
                    throw new ClassNotFoundException("invokedynamic is not allowed");
                }
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    boolean allowed = !methodInsn.owner.contains("/");
                    for (String allow : ALLOWED) {
                        if (methodInsn.owner.startsWith(allow)) {
                            allowed = true;
                            break;
                        }
                    }
                    for (String deny : DENIED) {
                        if (methodInsn.owner.startsWith(deny)) {
                            allowed = false;
                            break;
                        }
                    }
                    if (!allowed) {
                        throw new ClassNotFoundException("Call to " + methodInsn.owner + " is not allowed");
                    }
                }
            }
        }

        return this.defineClass(name, data, 0, data.length);
    }

    private byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];

        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }

        return out.toByteArray();
    }
}
