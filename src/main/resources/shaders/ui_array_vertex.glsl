#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 textureCoords;

uniform mat4 transformationMatrix;

out vec2 pass_textureCoords;

void main()
{
    pass_textureCoords = textureCoords;

    gl_Position = transformationMatrix * vec4(pos.xyz, 1.0);
}