/*
 *
 * ZNServersNPC
 * Copyright (C) 2019 Gaston Gonzalez (ZNetwork)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package ak.znetwork.znpcservers.commands.invoker;

import ak.znetwork.znpcservers.commands.exception.CommandPermissionException;
import ak.znetwork.znpcservers.commands.impl.ZNCommandAbstract;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandInvoker<T extends CommandSender> extends ZNCommandAbstract<T> {

    public CommandInvoker(final Object object, final Method method, final String permission) {
        super(object, method, permission);
    }

    @Override
    public void execute(T sender, Object object) throws CommandPermissionException {
        if (!sender.hasPermission(getPermission())) throw new CommandPermissionException("Insufficient permission");

        try {
            getMethod().invoke(getObject(), sender, object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CommandException("Command could not be executed", e);
        }
    }
}