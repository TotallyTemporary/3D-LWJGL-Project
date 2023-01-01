#version 330 core
in vec3 pass_colour;
out vec4 FragColor;

void main()
{
    FragColor = vec4(pass_colour.xyz, 1.0f);
} 