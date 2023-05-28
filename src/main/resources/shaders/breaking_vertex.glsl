#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 textureCoords;

out vec3 pass_textureCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main()
{
    pass_textureCoords = textureCoords;
    vec3 movedPosition = vec3(pos.x - 0.5, pos.y - 0.5, pos.z - 0.5);
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(movedPosition, 1.0);
}