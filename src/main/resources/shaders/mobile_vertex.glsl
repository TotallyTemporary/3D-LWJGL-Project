#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 textureCoords;

out vec2 pass_textureCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main()
{
    pass_textureCoords = textureCoords;
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(pos.xyz, 1.0);
}