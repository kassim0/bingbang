import {RouterModule, Routes} from '@angular/router';
import {TestApiComponent} from "./components/test-api/test-api.component";
import {NgModule} from "@angular/core";
import {HttpClientModule} from "@angular/common/http";
import {ApiModule, Configuration} from "./openApi";

export const routes: Routes = [
  {path: 'api', component: TestApiComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes),
    HttpClientModule,
    ApiModule.forRoot(() => new Configuration({ basePath: 'http://localhost:8080' }))],
  exports: [RouterModule]
})
export class AppRoutingModule{}
