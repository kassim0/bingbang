import {RouterModule, Routes} from '@angular/router';
import {TestApiComponent} from "./components/test-api/test-api.component";
import {NgModule} from "@angular/core";

export const routes: Routes = [
  {path: 'api', component: TestApiComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule{}
