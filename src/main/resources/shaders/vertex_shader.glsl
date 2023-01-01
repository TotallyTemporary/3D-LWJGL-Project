#version 330 core
layout (location = 0) in vec3 aPos;

out vec3 pass_colour;

void main()
{
    pass_colour = vec3(aPos.x, aPos.y, (aPos.x + aPos.y)/2);
    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
}