package shader;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Shader {

    private int program;

    public Shader(String vertexShaderPath, String fragmentShaderPath) {
        // compile shaders
        int vertexShader = -1, fragmentShader = -1;
        try {
            String vertexSource = Files.readString(Path.of(vertexShaderPath));
            vertexShader = compileShader(vertexSource, GL30.GL_VERTEX_SHADER);

            String fragmentSource = Files.readString(Path.of(fragmentShaderPath));
            fragmentShader = compileShader(fragmentSource, GL30.GL_FRAGMENT_SHADER);
        } catch (IOException e) {
            System.err.println("Problem creating shader:");
            e.printStackTrace();
        }

        // make program and link shaders
        int program = GL30.glCreateProgram();
        GL30.glAttachShader(program, vertexShader);
        GL30.glAttachShader(program, fragmentShader);
        GL30.glLinkProgram(program);

        // error checking
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var buf = stack.mallocInt(1);
            GL30.glGetProgramiv(program, GL30.GL_LINK_STATUS, buf);
            var status = buf.get();

            if (status != GL30.GL_TRUE) {
                String error = GL30.glGetProgramInfoLog(program);
                throw new IllegalStateException("Program linking failed " + status + ": " + error);
            }
        }

        this.program = program;
    }

    private int compileShader(String source, int type) {
        // create shader, set source, compile.
        var shader = GL30.glCreateShader(type);
        GL30.glShaderSource(shader, source);
        GL30.glCompileShader(shader);

        // do error checking.
        try (var stack = MemoryStack.stackPush()) {
            var buf = stack.mallocInt(1);
            GL30.glGetShaderiv(shader, GL30.GL_COMPILE_STATUS, buf);
            var status = buf.get();

            // something went wrong, get the error message
            if (status != GL30.GL_TRUE) {
                String error = GL30.glGetShaderInfoLog(shader);
                throw new IllegalStateException("Shader compilation error " + status + ": " + error);
            }
        }

        return shader;
    }
}
