/*
 * Copyright 2012-2013 Alfresco Software Limited.
 * 
 * Licensed under the GNU Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.gnu.org/licenses/agpl-3.0.html
 * 
 * If you do not wish to be bound to the terms of the AGPL v3.0, 
 * A commercial license may be obtained by contacting the author.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 * 
 */
package org.alfresco.extension.countersign.constraints;


import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;


public class MapConstraint extends BaseParameterConstraint
{
    private HashMap<String, String> cm = new HashMap<String, String>();

    public MapConstraint()
    {
    }


    public void setConstraintMap(Map<String, String> m)
    {
        cm.putAll(m);
    }


    public Map<String, String> getAllowableValues()
    {
        return cm;
    }


    @Override
    protected Map<String, String> getAllowableValuesImpl()
    {
        return cm;
    }
}
