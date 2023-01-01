#version 330 core
layout (location = 0) in vec3 aPos;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

out vec3 pass_colour;

void main()
{
    pass_colour = vec3(aPos.xy, (aPos.x + aPos.y)/2);
    gl_Position = projectionMatrix * transformationMatrix * vec4(aPos.xyz, 1.0);
}